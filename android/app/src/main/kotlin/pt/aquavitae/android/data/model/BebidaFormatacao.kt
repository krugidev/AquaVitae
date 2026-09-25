package pt.aquavitae.android.data.model

import java.util.Locale

/** PT-PT sempre (vírgula decimal), independente do locale do telemóvel — ver `CLAUDE.md`, "comparar números". */
val LocalePt: Locale = Locale("pt", "PT")

private val AnoNoFimRegex = Regex("\\s+(\\d{4})$")

/** O ano no fim do nome ("Barca Velha 2015" → "2015"), mostrado à parte no cartão — o nome em si fica intacto. */
fun BebidaSummary.anoNoNome(): String? = nome?.let { AnoNoFimRegex.find(it)?.groupValues?.get(1) }

/**
 * A linha de atributos do cartão (homepage e catálogo): categoria (+ tipo de vinho, se houver) e até 2 atributos, por
 * ordem de prioridade — `corpo`, depois `tanino` (só tinto) ou `acidez` (as outras categorias/tipos de vinho), depois
 * `doçura`. Cada bebida mostra só os que tem valor (atributo sem valor = não disponível, nunca inventado). Ex.:
 * "VINHO TINTO • CORPO ENCORPADO • TANINO ELEVADO" ou, sem corpo, "VINHO BRANCO • ACIDEZ 3 • DOÇURA 1".
 */
fun BebidaSummary.linhaAtributos(): String {
    val cabecalho = listOfNotNull(categoriaNome, tipo).joinToString(" ")
    val candidatos = listOfNotNull(
        corpo?.let { "CORPO $it" },
        if (tipo == "Tinto") tanino?.let { "TANINO $it" } else nivelAcidez?.let { "ACIDEZ $it" },
        nivelDocura?.let { "DOÇURA $it" },
    )
    return (listOf(cabecalho) + candidatos.take(2)).joinToString(" • ").uppercase(LocalePt)
}

fun formatarPrecoPt(valor: Double): String = String.format(LocalePt, "%.2f€", valor)

/** "1 review" ou "N reviews" (o singular estava a sair "1 reviews"). */
fun textoReviews(total: Int): String = if (total == 1) "1 review" else "$total reviews"
