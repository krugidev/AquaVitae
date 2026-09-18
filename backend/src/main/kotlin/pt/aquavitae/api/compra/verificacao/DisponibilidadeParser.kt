package pt.aquavitae.api.compra.verificacao

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

enum class Disponibilidade { EM_STOCK, SEM_STOCK, DESCONHECIDA }

// Lê a disponibilidade de uma página de produto a partir de dados estruturados que a maioria das
// lojas já publica para o Google Shopping: JSON-LD schema.org (Product.offers.availability) e,
// em fallback, meta tags Open Graph (product:availability). Sem nenhum dos dois devolve
// DESCONHECIDA — a página existe, mas não sabemos o stock; nunca se assume "sem stock".
object DisponibilidadeParser {

    private val mapper = ObjectMapper()

    private val blocoJsonLd = Regex(
        """<script[^>]*type\s*=\s*["']application/ld\+json["'][^>]*>(.*?)</script>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    private val tagMeta = Regex("""<meta\s[^>]*>""", RegexOption.IGNORE_CASE)

    private val emStock = setOf(
        "instock", "limitedavailability", "onlineonly", "instoreonly", "preorder", "presale", "backorder",
    )
    private val semStock = setOf("outofstock", "soldout", "discontinued", "oos")

    fun analisar(html: String): Disponibilidade {
        val blocos = blocoJsonLd.findAll(html).mapNotNull { lerJson(it.groupValues[1].trim()) }.toList()

        // Só o produto principal conta: páginas com "produtos relacionados" também trazem
        // Offers de outros artigos, que não dizem nada sobre este.
        val produto = blocos.firstNotNullOfOrNull { primeiroProduto(it) }
        val valores = mutableListOf<String>()
        if (produto != null) {
            recolherDisponibilidades(produto, valores)
        } else {
            blocos.forEach { recolherDisponibilidades(it, valores) }
        }
        classificar(valores)?.let { return it }

        return classificar(valoresMeta(html)) ?: Disponibilidade.DESCONHECIDA
    }

    private fun lerJson(texto: String): JsonNode? =
        try {
            mapper.readTree(texto)
        } catch (e: Exception) {
            // JSON-LD mal formado é comum (vírgulas a mais, etc.) — ignora-se, não é erro.
            null
        }

    private fun primeiroProduto(no: JsonNode): JsonNode? {
        if (no.isObject && ehProduto(no.get("@type"))) return no
        if (no.isContainerNode) {
            for (filho in no) primeiroProduto(filho)?.let { return it }
        }
        return null
    }

    private fun ehProduto(tipo: JsonNode?): Boolean = when {
        tipo == null -> false
        tipo.isTextual -> tipo.asText().substringAfterLast('/').equals("Product", ignoreCase = true)
        tipo.isArray -> tipo.any { ehProduto(it) }
        else -> false
    }

    private fun recolherDisponibilidades(no: JsonNode, destino: MutableList<String>) {
        if (no.isObject) {
            no.get("availability")?.let { valor ->
                when {
                    valor.isTextual -> destino += valor.asText()
                    valor.isObject -> valor.get("@id")?.takeIf { it.isTextual }?.let { destino += it.asText() }
                    valor.isArray -> valor.forEach { item -> if (item.isTextual) destino += item.asText() }
                }
            }
        }
        if (no.isContainerNode) no.forEach { recolherDisponibilidades(it, destino) }
    }

    private fun valoresMeta(html: String): List<String> =
        tagMeta.findAll(html).mapNotNull { tag ->
            val chave = atributo(tag.value, "property") ?: atributo(tag.value, "name")
            if (chave?.lowercase() in setOf("product:availability", "og:availability")) {
                atributo(tag.value, "content")
            } else {
                null
            }
        }.toList()

    private fun atributo(tag: String, nome: String): String? =
        Regex("""\b$nome\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE).find(tag)?.groupValues?.get(1)

    private fun classificar(valores: List<String>): Disponibilidade? {
        val normalizados = valores.map(::normalizar)
        return when {
            normalizados.any { it in emStock } -> Disponibilidade.EM_STOCK
            normalizados.any { it in semStock } -> Disponibilidade.SEM_STOCK
            else -> null
        }
    }

    // "https://schema.org/InStock", "schema:InStock", "in stock", "Out-of-Stock" -> "instock"/"outofstock"
    private fun normalizar(valor: String): String =
        valor.trim().substringAfterLast('/').substringAfterLast(':')
            .replace(" ", "").replace("-", "").replace("_", "").lowercase()
}
