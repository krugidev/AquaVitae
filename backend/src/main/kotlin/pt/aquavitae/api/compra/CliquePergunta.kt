package pt.aquavitae.api.compra

import java.time.Duration
import java.time.Instant

/** O que o utilizador pode responder ao "Compraste?" depois de clicar num link de compra. */
enum class RespostaClique { COMPREI, NAO_COMPREI }

/**
 * Quando é que se pergunta "chegaste a comprar?" (briefing, secção 4: "janela 24-72h"). Um clique só entra no inquérito
 * **depois de [aposMinutos]** — quem volta à app segundos depois do clique quase de certeza não comprou, e perguntar
 * logo seria ruído — e **até [ateHoras]** (passado esse tempo já ninguém se lembra). Ambos configuráveis
 * (`aquavitae.compra.pergunta-apos-minutos` / `pergunta-ate-horas`).
 */
object CliquePergunta {

    /** A janela `[desde, ate]` de datas de clique elegíveis, vista de `agora`. */
    fun janela(agora: Instant, aposMinutos: Long, ateHoras: Long): Pair<Instant, Instant> =
        agora.minus(Duration.ofHours(ateHoras)) to agora.minus(Duration.ofMinutes(aposMinutos))
}
