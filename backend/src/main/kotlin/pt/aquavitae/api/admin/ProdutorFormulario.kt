package pt.aquavitae.api.admin

import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URI

// Limites que vêm do schema (`produtor` em database/ddl/01_tables.sql): o formulário recusa antes de a BD dar ORA-12899.
const val NOME_PRODUTOR_MAX = 150
const val WEBSITE_MAX = 255
const val IMAGEM_PRODUTOR_MAX = 255
const val MORADA_MAX = 300
const val HISTORIA_MAX = 20_000
const val ANO_FUNDACAO_MIN = 1000

/**
 * O que o formulário do produtor devolve, **em texto e tal como foi escrito** (é o que se mostra outra vez se houver erros,
 * sem o "corrigir" por baixo). [ProdutorValidacao] transforma-o em [ProdutorDados] ou diz o que está mal, campo a campo.
 */
data class ProdutorFormulario(
    val nome: String = "",
    val paisId: String = "",
    val regiaoId: String = "",
    val anoFundacao: String = "",
    val website: String = "",
    val imagem: String = "",
    val morada: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val historia: String = "",
    val permiteVisitas: Boolean = false,
)

/** O formulário depois de validado e limpo (espaços cortados, vazios a `null`, números lidos). Falta só o que depende da BD. */
data class ProdutorDados(
    val nome: String,
    val paisId: Long,
    val regiaoId: Long?,
    val anoFundacao: Int?,
    val website: String?,
    val imagem: String?,
    val morada: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val historia: String?,
    val permiteVisitas: Boolean,
)

sealed interface ValidacaoProdutor {
    data class Valido(val dados: ProdutorDados) : ValidacaoProdutor

    /** Campo do formulário → mensagem (em PT-PT, para mostrar junto ao campo). */
    data class Invalido(val erros: Map<String, String>) : ValidacaoProdutor
}

/** As regras do formulário do produtor que não precisam da BD (as que precisam — país e região existentes, nome repetido — estão no serviço). */
object ProdutorValidacao {

    fun validar(form: ProdutorFormulario, anoAtual: Int): ValidacaoProdutor {
        val erros = linkedMapOf<String, String>()

        val nome = form.nome.trim().replace(Regex("\\s+"), " ")
        when {
            nome.isEmpty() -> erros["nome"] = "O nome é obrigatório."
            nome.length > NOME_PRODUTOR_MAX -> erros["nome"] = "O nome tem no máximo $NOME_PRODUTOR_MAX caracteres."
        }

        val paisId = form.paisId.trim().toLongOrNull()
        if (paisId == null) erros["paisId"] = "Escolhe o país."

        val regiaoTexto = form.regiaoId.trim()
        val regiaoId = if (regiaoTexto.isEmpty()) null else regiaoTexto.toLongOrNull()
        if (regiaoTexto.isNotEmpty() && regiaoId == null) erros["regiaoId"] = "Região inválida."

        val anoTexto = form.anoFundacao.trim()
        val ano = if (anoTexto.isEmpty()) null else anoTexto.toIntOrNull()
        if (anoTexto.isNotEmpty() && (ano == null || ano < ANO_FUNDACAO_MIN || ano > anoAtual)) {
            erros["anoFundacao"] = "O ano de fundação tem de estar entre $ANO_FUNDACAO_MIN e $anoAtual."
        }

        val website = form.website.trim()
        val websiteFinal = if (website.isEmpty()) null else normalizarWebsite(website)
        if (website.isNotEmpty() && websiteFinal == null) {
            erros["website"] = "O website não é um endereço válido (ex.: https://www.exemplo.pt)."
        } else if (websiteFinal != null && websiteFinal.length > WEBSITE_MAX) {
            erros["website"] = "O website tem no máximo $WEBSITE_MAX caracteres."
        }

        val imagem = form.imagem.trim()
        val imagemFinal = if (imagem.isEmpty()) null else imagem
        if (imagemFinal != null && !imagemValida(imagemFinal)) {
            erros["imagem"] = "A imagem tem de ser um URL https://… ou um caminho a começar por /."
        } else if (imagemFinal != null && imagemFinal.length > IMAGEM_PRODUTOR_MAX) {
            erros["imagem"] = "O endereço da imagem tem no máximo $IMAGEM_PRODUTOR_MAX caracteres."
        }

        val morada = form.morada.trim().replace(Regex("\\s+"), " ")
        if (morada.length > MORADA_MAX) erros["morada"] = "A morada tem no máximo $MORADA_MAX caracteres."

        val latitude = lerCoordenada(form.latitude, -90, 90, "latitude", erros)
        val longitude = lerCoordenada(form.longitude, -180, 180, "longitude", erros)
        if ((latitude == null) != (longitude == null) && "latitude" !in erros && "longitude" !in erros) {
            erros[if (latitude == null) "latitude" else "longitude"] = "Preenche a latitude e a longitude, ou nenhuma das duas."
        }

        val historia = form.historia.replace("\r\n", "\n").trim()
        if (historia.length > HISTORIA_MAX) erros["historia"] = "A história tem no máximo $HISTORIA_MAX caracteres."

        if (erros.isNotEmpty() || paisId == null) return ValidacaoProdutor.Invalido(erros)
        return ValidacaoProdutor.Valido(
            ProdutorDados(
                nome = nome,
                paisId = paisId,
                regiaoId = regiaoId,
                anoFundacao = ano,
                website = websiteFinal,
                imagem = imagemFinal,
                morada = morada.ifEmpty { null },
                latitude = latitude,
                longitude = longitude,
                historia = historia.ifEmpty { null },
                permiteVisitas = form.permiteVisitas,
            ),
        )
    }

    // "www.exemplo.pt" → "https://www.exemplo.pt" (escrever o esquema à mão é chato); só http e https, com um domínio a sério.
    // `null` se não for um endereço aceitável. Nunca deixa passar "javascript:", "data:", etc.
    fun normalizarWebsite(texto: String): String? {
        val comEsquema = if ("://" in texto) texto else "https://$texto"
        if (comEsquema.any { it.isWhitespace() }) return null
        val uri = try {
            URI(comEsquema)
        } catch (e: Exception) {
            return null
        }
        val esquema = uri.scheme?.lowercase()
        val anfitriao = uri.host
        if ((esquema != "http" && esquema != "https") || anfitriao.isNullOrBlank() || '.' !in anfitriao) return null
        return comEsquema
    }

    // Um URL absoluto https:// (ou http://) ou um caminho de um recurso estático do backend (a começar por "/").
    private fun imagemValida(texto: String): Boolean =
        texto.none { it.isWhitespace() } &&
            ((texto.startsWith("/") && !texto.startsWith("//")) || ("://" in texto && normalizarWebsite(texto) != null))

    // Aceita "41,1621" e "41.1621" (o teclado português escreve vírgula). NUMBER(9,6): 6 casas decimais. `null` se vazio ou inválido
    // (o segundo caso deixa a mensagem em `erros`).
    private fun lerCoordenada(texto: String, minimo: Int, maximo: Int, campo: String, erros: MutableMap<String, String>): BigDecimal? {
        val limpo = texto.trim().replace(',', '.')
        if (limpo.isEmpty()) return null
        val valor = if (Regex("[-+]?\\d+(\\.\\d+)?").matches(limpo)) limpo.toBigDecimalOrNull() else null
        if (valor == null || valor < BigDecimal(minimo) || valor > BigDecimal(maximo)) {
            erros[campo] = "A $campo tem de ser um número entre $minimo e $maximo (ex.: 41,1621)."
            return null
        }
        return valor.setScale(6, RoundingMode.HALF_UP)
    }
}
