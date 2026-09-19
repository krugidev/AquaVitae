package pt.aquavitae.api.produtor

import pt.aquavitae.api.lookup.comparadorAlfabeticoPt
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// "Produtor da semana" (homepage): computado, sem coluna nem curadoria manual (decisão de 2026-09-17).
// Ordena os produtores com bebidas por rating médio (desc; nome e id desempatam) e roda, uma posição por
// semana, pelos TAMANHO_ROTACAO primeiros — rodar por todos poria na homepage, a meio do ano, produtores
// sem qualquer avaliação. Cada segunda-feira passa ao seguinte, sem saltos na mudança de ano.
// Limitação conhecida: o ranking usa os ratings de agora, por isso uma review nova pode mudar o produtor a meio
// da semana; se isso incomodar, guarda-se a escolha por semana.
object ProdutorDestaque {

    const val TAMANHO_ROTACAO = 10

    data class Candidato(val id: Long, val nome: String?, val ratingMedio: Double)

    // 1970-01-05 foi uma segunda-feira: as semanas contam-se a partir daí.
    private val SEGUNDA_DE_REFERENCIA = LocalDate.of(1970, 1, 5)

    fun escolher(candidatos: List<Candidato>, hoje: LocalDate): Candidato? {
        if (candidatos.isEmpty()) return null
        val ranking = candidatos
            .sortedWith(
                compareByDescending<Candidato> { it.ratingMedio }
                    .thenBy(comparadorAlfabeticoPt) { it.nome }
                    .thenBy { it.id },
            )
            .take(TAMANHO_ROTACAO)
        val semanas = ChronoUnit.WEEKS.between(SEGUNDA_DE_REFERENCIA, hoje)
        return ranking[Math.floorMod(semanas, ranking.size.toLong()).toInt()]
    }
}
