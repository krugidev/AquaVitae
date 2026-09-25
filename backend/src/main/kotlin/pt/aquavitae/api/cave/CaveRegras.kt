package pt.aquavitae.api.cave

import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.PedidoInvalidoException
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

// Onde está cada garrafa em relação à sua janela de consumo. Calculado ao ler, nunca guardado (muda com o
// calendário).
enum class EstadoCaveBebida {
    // Ainda não é altura: a janela não começou — ou não há janela definida (sem janela não há "pronta").
    EM_GUARDA,

    // Dentro da janela (extremos incluídos), ou com janela só de início já começada / só de fim ainda por chegar.
    PRONTA,

    // A janela acabou e a garrafa continua por consumir: fica na lista "prontas a abrir", com aviso "Em atraso".
    EM_ATRASO,

    // Já consumida (fica como histórico, fora das listas da cave).
    CONSUMIDA,
    ;

    companion object {
        fun de(caveBebida: CaveBebida, hoje: LocalDate) =
            calcular(caveBebida.isConsumida, caveBebida.janelaInicio, caveBebida.janelaFim, hoje)

        fun calcular(consumida: Boolean, inicio: LocalDate?, fim: LocalDate?, hoje: LocalDate): EstadoCaveBebida = when {
            consumida -> CONSUMIDA
            inicio == null && fim == null -> EM_GUARDA
            fim != null && hoje.isAfter(fim) -> EM_ATRASO
            inicio != null && hoje.isBefore(inicio) -> EM_GUARDA
            else -> PRONTA
        }
    }
}

// Ordenação das listas da cave (?sort=): `preco` (preço por unidade, o mais caro primeiro) ou `dataConsumo`
// (o que deve ser bebido primeiro). Sem `sort`, `dataConsumo`.
enum class CaveOrdenacao {
    PRECO,
    DATA_CONSUMO,
    ;

    companion object {
        fun de(valor: String?): CaveOrdenacao = when (valor?.trim()?.lowercase()) {
            null, "", "dataconsumo" -> DATA_CONSUMO
            "preco" -> PRECO
            else -> throw PedidoInvalidoException("sort inválido: use preco ou dataConsumo")
        }
    }
}

data class CaveResumo(
    val totalGarrafas: Int,
    val valorTotal: BigDecimal,
    // Garrafas prontas a abrir, incluindo as em atraso (estão na mesma lista).
    val totalProntasAAbrir: Int,
)

// Regras de negócio das caves, sem acesso à BD (por isso testáveis em isolamento).
object CaveRegras {

    // Só conta garrafas por consumir. O valor é o que o utilizador registou ter pago por unidade (preço pago x
    // quantidade); garrafas sem preço registado não entram no valor.
    fun resumir(linhas: List<CaveBebida>, hoje: LocalDate): CaveResumo {
        val ativas = linhas.filter { !it.isConsumida }
        return CaveResumo(
            totalGarrafas = ativas.sumOf { it.quantidade },
            valorTotal = ativas
                .mapNotNull { linha -> linha.precoPago?.multiply(BigDecimal(linha.quantidade)) }
                .fold(BigDecimal.ZERO.setScale(2)) { total, valor -> total + valor },
            totalProntasAAbrir = ativas
                .filter { EstadoCaveBebida.de(it, hoje) != EstadoCaveBebida.EM_GUARDA }
                .sumOf { it.quantidade },
        )
    }

    // Prontas (e em atraso): quem tem o prazo mais próximo — ou já passado — vem primeiro; sem prazo, no fim.
    fun ordenarProntas(linhas: List<CaveBebida>, ordenacao: CaveOrdenacao): List<CaveBebida> = when (ordenacao) {
        CaveOrdenacao.PRECO -> linhas.sortedWith(porPrecoDesc)
        CaveOrdenacao.DATA_CONSUMO -> linhas.sortedWith(
            compareBy<CaveBebida, LocalDate?>(datasNullsLast) { it.janelaFim }
                .thenBy(datasNullsLast) { it.janelaInicio }
                .thenBy { it.id },
        )
    }

    // Em guarda: quem vai ficar pronto primeiro (início da janela) vem primeiro; sem janela, no fim.
    fun ordenarEmGuarda(linhas: List<CaveBebida>, ordenacao: CaveOrdenacao): List<CaveBebida> = when (ordenacao) {
        CaveOrdenacao.PRECO -> linhas.sortedWith(porPrecoDesc)
        CaveOrdenacao.DATA_CONSUMO -> linhas.sortedWith(
            compareBy<CaveBebida, LocalDate?>(datasNullsLast) { it.janelaInicio }
                .thenBy(datasNullsLast) { it.janelaFim }
                .thenBy { it.id },
        )
    }

    fun validarJanela(inicio: LocalDate?, fim: LocalDate?) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw PedidoInvalidoException("A janela de consumo não pode acabar antes de começar")
        }
    }

    // Resultado de consumir uma garrafa. `consumida` é a linha que fica como histórico: a própria linha se só
    // havia uma garrafa, ou uma linha nova (ainda por gravar, id = 0) se havia mais.
    data class Consumo(val restantes: Int, val consumida: CaveBebida)

    // Consumir é sempre UMA garrafa: com várias, a linha perde uma e o histórico fica numa linha própria, com a
    // data de consumo; com uma só, a linha passa a consumida. Não grava nada: quem chama grava `linha` e, se for
    // diferente, `consumida`.
    fun consumirUma(linha: CaveBebida, dataConsumo: LocalDate, notas: String?, agora: Instant): Consumo {
        if (linha.isConsumida) throw ConflictException("Esta garrafa já foi consumida")

        if (linha.quantidade <= 1) {
            linha.isConsumida = true
            linha.dataConsumo = dataConsumo
            notas?.let { linha.notas = it }
            return Consumo(restantes = 0, consumida = linha)
        }

        linha.quantidade -= 1
        val consumida = CaveBebida(
            cave = linha.cave,
            bebida = linha.bebida,
            quantidade = 1,
            dataAquisicao = linha.dataAquisicao,
            precoPago = linha.precoPago,
            janelaInicio = linha.janelaInicio,
            janelaFim = linha.janelaFim,
            isConsumida = true,
            dataConsumo = dataConsumo,
            notas = notas,
            dataCriacao = agora,
        )
        return Consumo(restantes = linha.quantidade, consumida = consumida)
    }

    private val datasNullsLast: Comparator<LocalDate?> = Comparator.nullsLast(Comparator.naturalOrder())

    private val porPrecoDesc: Comparator<CaveBebida> =
        compareBy<CaveBebida, BigDecimal?>(Comparator.nullsLast(Comparator.reverseOrder())) { it.precoPago }
            .thenBy { it.id }
}
