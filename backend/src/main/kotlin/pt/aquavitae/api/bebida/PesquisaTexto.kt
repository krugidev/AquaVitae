package pt.aquavitae.api.bebida

/**
 * O texto da pesquisa do catálogo, sem acentos e sem curingas do `LIKE` (pedido em 2026-09-24: "esporao", "meao" e "beirao não
 * encontravam nada — só "Esporão", "Meão" e "Beirão" — e um `%` ou `_` escrito na pesquisa devolvia tudo).
 *
 * **Como funciona:** do lado da BD a query compara `TRANSLATE(UPPER(coluna), COM_ACENTO, SEM_ACENTO)` com o padrão; do lado do
 * Kotlin o termo passa pelo **mesmo mapa** ([normalizar]) — é por isso que os dois textos vivem aqui, em constantes que a query
 * (`BebidaRepository.search`) usa por interpolação: uma só fonte de verdade. **Não se mexe em `NLS_COMP`/`NLS_SORT`** (o modo
 * "insensível a acentos" do Oracle): mudava todas as comparações de texto da BD (logins, unicidade, seeds), não só a pesquisa.
 */
object PesquisaTexto {

    /** As maiúsculas acentuadas (latim: português, espanhol, francês, alemão, italiano) — `UPPER` trata das minúsculas. */
    const val COM_ACENTO = "ÁÀÂÃÄÅÇÉÈÊËÍÌÎÏÑÓÒÔÕÖÚÙÛÜÝŸ"

    /** A letra base de cada uma de [COM_ACENTO], na mesma posição (o `TRANSLATE` do Oracle troca carácter a carácter). */
    const val SEM_ACENTO = "AAAAAACEEEEIIIINOOOOOUUUUYY"

    /** Um carácter que o `LIKE` precisa de ter escapado; usado com `ESCAPE '!'` na query. */
    private const val ESCAPE = '!'

    /**
     * O texto como a BD o vai comparar: maiúsculas (1 para 1, como o `UPPER` do Oracle — `String.uppercase()` faria "ß" → "SS")
     * e sem acentos.
     */
    fun normalizar(texto: String): String = buildString(texto.length) {
        for (c in texto) {
            val maiuscula = c.uppercaseChar()
            val i = COM_ACENTO.indexOf(maiuscula)
            append(if (i >= 0) SEM_ACENTO[i] else maiuscula)
        }
    }

    /** Escapa `!`, `%` e `_`: o que o utilizador escreve é texto, não um curinga do `LIKE`. */
    fun escaparLike(texto: String): String = buildString(texto.length) {
        for (c in texto) {
            if (c == ESCAPE || c == '%' || c == '_') append(ESCAPE)
            append(c)
        }
    }

    /**
     * O padrão do `LIKE` para uma pesquisa "contém": `%TEXTO%`, sem acentos, em maiúsculas e com os curingas escapados.
     * `null` se não há texto (em branco), que é "sem filtro". Espaços nas pontas não contam (o teclado do telemóvel deixa-os).
     */
    fun padraoContem(pesquisa: String?): String? {
        val texto = pesquisa?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return "%" + escaparLike(normalizar(texto)) + "%"
    }
}
