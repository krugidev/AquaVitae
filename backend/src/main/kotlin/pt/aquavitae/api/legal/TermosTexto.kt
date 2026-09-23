package pt.aquavitae.api.legal

import org.springframework.web.util.HtmlUtils

/** Um pedaço do texto dos termos: um título de secção ou um parágrafo. */
data class TermosBloco(val tipo: String, val texto: String)

/** `GET /api/legal/termos`: o texto dos termos e condições, já dividido em blocos para a app os desenhar. */
data class TermosDto(val titulo: String, val blocos: List<TermosBloco>)

/**
 * O texto dos termos e condições vive num ficheiro simples (`src/main/resources/legal/termos.txt`) que se edita à mão. Uma só
 * fonte serve a app (o popup "Termos e Condições", em JSON) e a página pública (`/legal/termos.html`).
 *
 * Formato: uma linha que começa por `# ` é o título de uma secção; o resto são parágrafos, separados por linhas em branco (as
 * quebras de linha dentro de um parágrafo contam como espaços). Sem mais nada: sem HTML nem formatação.
 */
object TermosTexto {
    const val TITULO = "Termos e Condições"
    const val TIPO_TITULO = "titulo"
    const val TIPO_PARAGRAFO = "paragrafo"

    fun analisar(texto: String): List<TermosBloco> {
        val blocos = mutableListOf<TermosBloco>()
        val paragrafo = mutableListOf<String>()
        fun fecharParagrafo() {
            if (paragrafo.isNotEmpty()) {
                blocos += TermosBloco(TIPO_PARAGRAFO, paragrafo.joinToString(" "))
                paragrafo.clear()
            }
        }
        for (linha in texto.lines().map { it.trim() }) {
            when {
                linha.isEmpty() -> fecharParagrafo()
                linha.startsWith("# ") -> {
                    fecharParagrafo()
                    blocos += TermosBloco(TIPO_TITULO, linha.removePrefix("# ").trim())
                }
                else -> paragrafo += linha
            }
        }
        fecharParagrafo()
        return blocos
    }

    /** A página pública dos termos (o texto vai escapado: nada do ficheiro é interpretado como HTML). */
    fun paraHtml(titulo: String, blocos: List<TermosBloco>): String {
        // htmlEscape com UTF-8: só escapa os caracteres especiais do HTML; os acentos ficam como estão.
        val corpo = blocos.joinToString("\n  ") { bloco ->
            val texto = HtmlUtils.htmlEscape(bloco.texto, "UTF-8")
            if (bloco.tipo == TIPO_TITULO) "<h2>$texto</h2>" else "<p>$texto</p>"
        }
        val tituloHtml = HtmlUtils.htmlEscape(titulo, "UTF-8")
        return """
            <!DOCTYPE html>
            <html lang="pt-PT">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>$tituloHtml — AquaVitae</title>
              <style>
                body { font-family: system-ui, sans-serif; line-height: 1.6; margin: 0 auto; padding: 24px 20px 48px; max-width: 720px; color: #1B1714; background: #fff; }
                h1 { font-size: 1.5rem; margin: 0 0 20px; color: #8F321D; }
                h2 { font-size: 1.1rem; margin: 28px 0 8px; }
                p { margin: 0 0 12px; }
              </style>
            </head>
            <body>
              <h1>$tituloHtml</h1>
              $corpo
            </body>
            </html>
        """.trimIndent()
    }
}
