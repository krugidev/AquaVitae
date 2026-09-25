package pt.aquavitae.android.feature.perfil

import pt.aquavitae.android.data.model.LocalePt
import pt.aquavitae.android.data.model.MinhaReview
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle

// O mês de uma review conta-se na hora de Portugal (uma review escrita às 23:30 de 31 de agosto não passa a setembro por causa do UTC).
private val Lisboa: ZoneId = ZoneId.of("Europe/Lisbon")

/** O tamanho do excerto da review na lista (o mockup: "primeiros ~90 caracteres"). */
const val EXCERTO_MAX = 90

/**
 * O excerto do comentário: os primeiros [EXCERTO_MAX] caracteres, cortados na última palavra inteira e com "…" se houve corte.
 * `null` se a review é só estrelas (sem comentário).
 */
fun excertoDaReview(comentario: String?): String? {
    val texto = comentario?.trim()?.replace(Regex("\\s+"), " ")?.takeIf { it.isNotEmpty() } ?: return null
    if (texto.length <= EXCERTO_MAX) return texto
    val corte = texto.substring(0, EXCERTO_MAX)
    val ultimoEspaco = corte.lastIndexOf(' ')
    // Um único "palavrão" sem espaços: corta a direito.
    val base = if (ultimoEspaco > EXCERTO_MAX / 2) corte.substring(0, ultimoEspaco) else corte
    return base.trimEnd(' ', ',', ';', ':', '.', '-') + "…"
}

/** Um mês com reviews: serve de chave de agrupamento (ordenável, mais recente primeiro) e de pílula do filtro. */
data class MesDeReviews(val ano: Int, val mes: Int) : Comparable<MesDeReviews> {
    override fun compareTo(other: MesDeReviews) = compareValuesBy(this, other, { it.ano }, { it.mes })

    private val nome: String get() = java.time.Month.of(mes).getDisplayName(TextStyle.FULL, LocalePt)

    /** O cabeçalho do grupo: "SETEMBRO 2026". */
    val cabecalho: String get() = "$nome $ano".uppercase(LocalePt)

    /** A pílula: "Setembro"; com o ano ("Setembro 2025") quando não é o do mês mais recente. */
    fun pilula(anoDeReferencia: Int): String {
        val base = nome.replaceFirstChar { it.uppercase(LocalePt) }
        return if (ano == anoDeReferencia) base else "$base $ano"
    }
}

/** O mês (na hora de Portugal) de uma data ISO (`createdAt`); `null` se não vem ou não se lê. */
fun mesDaReview(dataIso: String?): MesDeReviews? = runCatching {
    val data = Instant.parse(dataIso).atZone(Lisboa)
    MesDeReviews(data.year, data.monthValue)
}.getOrNull()

/** "18 SET" — o dia e o mês abreviado (na hora de Portugal), em maiúsculas, como no rodapé do cartão do mockup. */
fun dataCurtaDaReview(dataIso: String?): String = runCatching {
    val data = Instant.parse(dataIso).atZone(Lisboa)
    val mes = data.month.getDisplayName(TextStyle.SHORT, LocalePt).trimEnd('.').uppercase(LocalePt)
    "${data.dayOfMonth} $mes"
}.getOrDefault("")

/** Um grupo da lista: o mês e as suas reviews (mais recentes primeiro, como vêm da API). */
data class GrupoDeReviews(val mes: MesDeReviews?, val reviews: List<MinhaReview>) {
    val cabecalho: String get() = mes?.cabecalho ?: "SEM DATA"
}

/** Agrupa por mês, do mais recente para o mais antigo (reviews sem data no fim). */
fun agruparPorMes(reviews: List<MinhaReview>): List<GrupoDeReviews> =
    reviews.groupBy { mesDaReview(it.createdAt) }
        .map { (mes, lista) -> GrupoDeReviews(mes, lista) }
        .sortedWith(compareByDescending<GrupoDeReviews> { it.mes != null }.thenByDescending { it.mes })

/** Os meses que têm reviews, do mais recente para o mais antigo (as pílulas do filtro depois de "Todos"). */
fun mesesComReviews(reviews: List<MinhaReview>): List<MesDeReviews> =
    reviews.mapNotNull { mesDaReview(it.createdAt) }.distinct().sortedDescending()
