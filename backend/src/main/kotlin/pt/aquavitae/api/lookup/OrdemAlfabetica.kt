package pt.aquavitae.api.lookup

import java.text.Collator
import java.util.Locale

// O Oracle ordena por código de carácter (binário), o que põe "África do Sul", "Índia" ou "Óbidos" depois
// de "Zimbábue" ou "Vinho Verde". As listas grandes que a app mostra por ordem alfabética (países, castas,
// regiões) ordenam-se aqui, com o collator de português. As pequenas (corpo, tanino, tipo, ...) mantêm a
// ordem por id, que é a pretendida (Leve, Médio, Encorpado) — não passar por aqui.
private val collator: Collator = Collator.getInstance(Locale.forLanguageTag("pt-PT"))

// Nulos contam como texto vazio (ficam primeiro).
val comparadorAlfabeticoPt: Comparator<String?> = Comparator { a, b -> collator.compare(a.orEmpty(), b.orEmpty()) }

fun <T> List<T>.ordenadoPorNome(nome: (T) -> String?): List<T> = sortedWith(compareBy(comparadorAlfabeticoPt, nome))

// O país do mercado da app (decisão de produto, 2026-09-20): fica sempre no topo da lista de países.
const val PAIS_EM_DESTAQUE = "Portugal"

// `destaque` (se existir na lista) vai primeiro; os restantes seguem por ordem alfabética.
fun <T> List<T>.ordenadoPorNomeComDestaque(destaque: String, nome: (T) -> String?): List<T> {
    val (primeiros, resto) = partition { nome(it) == destaque }
    return primeiros + resto.ordenadoPorNome(nome)
}
