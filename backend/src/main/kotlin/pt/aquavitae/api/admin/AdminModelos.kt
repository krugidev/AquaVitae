package pt.aquavitae.api.admin

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val PT = Locale.forLanguageTag("pt-PT")

/** Os filtros de "qualidade do catálogo" das bebidas: o que ainda falta preencher. Só um de cada vez (mais o resto dos filtros). */
enum class QualidadeBebida(val codigo: String, val rotulo: String) {
    SEM_IMAGEM("sem-imagem", "Sem imagem"),
    SEM_PRODUTOR("sem-produtor", "Sem produtor"),
    SEM_EAN("sem-ean", "Sem EAN"),
    SEM_LINK("sem-link", "Sem link de compra ativo"),
    DADOS_GERAIS("dados-gerais", "Dados gerais em falta (categoria, país, teor, volume)"),
    ATRIBUTOS_VINHO("atributos-vinho", "Vinho sem tipo ou sem castas"),
    ;

    companion object {
        fun deCodigo(codigo: String?): QualidadeBebida? = entries.firstOrNull { it.codigo == codigo }
    }
}

/** O mesmo para os produtores. */
enum class QualidadeProdutor(val codigo: String, val rotulo: String) {
    SEM_IMAGEM("sem-imagem", "Sem imagem"),
    SEM_HISTORIA("sem-historia", "Sem história"),
    SEM_MORADA("sem-morada", "Sem morada"),
    SEM_COORDENADAS("sem-coordenadas", "Sem coordenadas"),
    SEM_REGIAO("sem-regiao", "Sem região"),
    SEM_BEBIDAS("sem-bebidas", "Sem nenhuma bebida"),
    ;

    companion object {
        fun deCodigo(codigo: String?): QualidadeProdutor? = entries.firstOrNull { it.codigo == codigo }
    }
}

/** Uma linha da lista de bebidas (sem carregar entidades: uma só query, sem N+1). */
data class BebidaAdminLinha(
    val id: Long,
    val nome: String?,
    val categoria: String?,
    val produtor: String?,
    val pais: String?,
    val ean: String?,
    val imagem: String?,
    val ano: Int?,
    val teor: BigDecimal?,
    val volumeMl: BigDecimal?,
    val linksAtivos: Long,
) {
    val imagemUrl: String? get() = urlDeImagem(imagem)
    val detalhes: String get() = listOfNotNull(ano?.toString(), teor?.let { "${numero(it)}%" }, volumeMl?.let { "${numero(it)} ml" }).joinToString(" · ")

    // O que falta, para os avisos da lista (o mesmo critério dos filtros).
    val problemas: List<String>
        get() = buildList {
            if (imagem.isNullOrBlank()) add("sem imagem")
            if (produtor == null) add("sem produtor")
            if (ean == null) add("sem EAN")
            if (linksAtivos == 0L) add("sem link ativo")
            if (categoria == null || pais == null || teor == null || volumeMl == null) add("dados em falta")
        }
}

/** Uma linha da lista de produtores. `historiaPreenchida` é 0/1 (a história é um CLOB: não se traz para a lista). */
data class ProdutorAdminLinha(
    val id: Long,
    val nome: String?,
    val pais: String?,
    val regiao: String?,
    val anoFundacao: Int?,
    val website: String?,
    val imagem: String?,
    val morada: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val historiaPreenchida: Int,
    val totalBebidas: Long,
) {
    val imagemUrl: String? get() = urlDeImagem(imagem)
    val temHistoria: Boolean get() = historiaPreenchida != 0

    val problemas: List<String>
        get() = buildList {
            if (imagem.isNullOrBlank()) add("sem imagem")
            if (!temHistoria) add("sem história")
            if (morada.isNullOrBlank()) add("sem morada")
            if (latitude == null || longitude == null) add("sem coordenadas")
            if (regiao == null) add("sem região")
            if (totalBebidas == 0L) add("sem bebidas")
        }
}

/** Uma página de resultados, com o que a paginação da lista precisa (páginas a partir de 0). */
data class PaginaAdmin<T>(
    val itens: List<T>,
    val pagina: Int,
    val totalPaginas: Int,
    val total: Long,
) {
    val temAnterior: Boolean get() = pagina > 0
    val temSeguinte: Boolean get() = pagina + 1 < totalPaginas
    val paginaParaMostrar: Int get() = pagina + 1
}

const val TAMANHO_PAGINA_ADMIN = 25

// A imagem de uma bebida/produtor é um URL absoluto (retalhista/feed) ou um caminho de um recurso estático do backend.
fun urlDeImagem(caminho: String?): String? {
    val valor = caminho?.trim().orEmpty()
    return when {
        valor.isEmpty() -> null
        valor.startsWith("http://") || valor.startsWith("https://") -> valor
        valor.startsWith("/") -> valor
        else -> "/$valor"
    }
}

// "13,5" (vírgula decimal, sem zeros a mais: "750" e não "750,0").
private fun numero(valor: BigDecimal): String =
    NumberFormat.getNumberInstance(PT).apply { maximumFractionDigits = 2 }.format(valor)
