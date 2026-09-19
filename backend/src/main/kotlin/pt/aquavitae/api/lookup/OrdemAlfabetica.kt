package pt.aquavitae.api.lookup

import java.text.Collator
import java.util.Locale

// O Oracle ordena por código de carácter (binário), o que põe "África do Sul", "Índia" ou "Óbidos" depois
// de "Zimbábue" ou "Vinho Verde". As listas grandes que a app mostra por ordem alfabética (países, castas,
// regiões) ordenam-se aqui, com o collator de português. As pequenas (corpo, tanino, tipo, ...) mantêm a
// ordem por id, que é a pretendida (Leve, Médio, Encorpado) — não passar por aqui.
private val collator: Collator = Collator.getInstance(Locale.forLanguageTag("pt-PT"))

fun <T> List<T>.ordenadoPorNome(nome: (T) -> String?): List<T> =
    sortedWith { a, b -> collator.compare(nome(a).orEmpty(), nome(b).orEmpty()) }
