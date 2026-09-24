package pt.aquavitae.android.data.model

import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

/** O URL só serve para abrir se não está em branco (uma oferta sem URL não tem botão). */
val OfertaCompra.temLink: Boolean get() = !url.isNullOrBlank()

/**
 * A oferta que o botão "Comprar" principal abre: a **mais barata das disponíveis** com link. A API já devolve as
 * disponíveis primeiro e por preço, mas não se depende disso: escolhe-se o menor preço (as sem preço ficam para o
 * fim) e, a igualdade, a primeira da lista.
 */
fun List<OfertaCompra>.maisBarataDisponivel(): OfertaCompra? =
    filter { it.disponivel && it.temLink }.minWithOrNull(compareBy<OfertaCompra> { it.preco ?: Double.MAX_VALUE })

/** Há mais de uma oferta disponível — só então "MAIS BARATO" e a comparação de preços fazem sentido. */
val List<OfertaCompra>.temComparacao: Boolean get() = count { it.disponivel } > 1

private fun instanteOuNulo(texto: String?): Instant? = try {
    texto?.let { Instant.parse(it) }
} catch (_: DateTimeParseException) {
    null
}

/** "há 3 dias", "há 5 horas"… a partir de um instante ISO (PT-PT). `null` se não há data. */
fun textoHa(instanteIso: String?, agora: Instant = Instant.now()): String? {
    val instante = instanteOuNulo(instanteIso) ?: return null
    val decorrido = Duration.between(instante, agora).coerceAtLeast(Duration.ZERO)
    val minutos = decorrido.toMinutes()
    val horas = decorrido.toHours()
    val dias = decorrido.toDays()
    return when {
        minutos < 1 -> "agora mesmo"
        minutos < 60 -> "há $minutos min"
        horas < 24 -> "há $horas ${if (horas == 1L) "hora" else "horas"}"
        dias == 1L -> "ontem"
        dias < 14 -> "há $dias dias"
        dias < 60 -> "há ${dias / 7} semanas"
        else -> "há ${dias / 30} meses"
    }
}

/** "atualizado hoje" / "atualizado há 3 dias" — o que se mostra por baixo do preço de uma oferta. */
fun OfertaCompra.atualizadoTexto(agora: Instant = Instant.now()): String? {
    val instante = instanteOuNulo(precoAtualizadoEm) ?: return null
    val decorrido = Duration.between(instante, agora)
    if (!decorrido.isNegative && decorrido.toHours() < 24) return "atualizado hoje"
    return textoHa(precoAtualizadoEm, agora)?.let { "atualizado $it" }
}
