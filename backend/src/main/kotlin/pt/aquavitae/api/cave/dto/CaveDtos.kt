package pt.aquavitae.api.cave.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import pt.aquavitae.api.cave.Cave
import pt.aquavitae.api.cave.CaveBebida
import pt.aquavitae.api.cave.CaveResumo
import pt.aquavitae.api.cave.EstadoCaveBebida
import java.math.BigDecimal
import java.time.LocalDate

data class CaveRequest(
    @field:NotBlank
    val nome: String,
    val descricao: String? = null,
)

// Lista "As minhas caves": os totais são só de garrafas por consumir. `valorTotal` é o preço que o utilizador
// registou ter pago (por unidade) x quantidade. `totalProntasAAbrir` são garrafas (não linhas) prontas ou em atraso.
// `temBebida` só vem preenchido com `?bebidaId=` no pedido (o popup "Adicionar à cave" do mockup: destacar as caves
// onde aquela bebida já está) — sem esse parâmetro fica sempre `false`, não "não sei".
data class CaveResponse(
    val id: Long,
    val nome: String?,
    val descricao: String?,
    val totalGarrafas: Int,
    val valorTotal: BigDecimal,
    val totalProntasAAbrir: Int,
    val temBebida: Boolean = false,
) {
    companion object {
        fun from(cave: Cave, resumo: CaveResumo, temBebida: Boolean = false) = CaveResponse(
            id = cave.id,
            nome = cave.nome,
            descricao = cave.descricao,
            totalGarrafas = resumo.totalGarrafas,
            valorTotal = resumo.valorTotal,
            totalProntasAAbrir = resumo.totalProntasAAbrir,
            temBebida = temBebida,
        )
    }
}

// `prontasAAbrir` inclui as em atraso (cada item traz o `estado`); as já consumidas não aparecem em nenhuma lista.
data class CaveDetailResponse(
    val id: Long,
    val nome: String?,
    val descricao: String?,
    val totalGarrafas: Int,
    val valorTotal: BigDecimal,
    val totalProntasAAbrir: Int,
    val prontasAAbrir: List<CaveBebidaResponse>,
    val emGuarda: List<CaveBebidaResponse>,
)

data class CaveBebidaRequest(
    @field:NotNull
    val bebidaId: Long,
    @field:Positive
    val quantidade: Int = 1,
    val precoPago: BigDecimal? = null,
    val dataAquisicao: LocalDate? = null,
    // Janela em que a garrafa deve ser bebida. Sem janela, fica "em guarda".
    val janelaInicio: LocalDate? = null,
    val janelaFim: LocalDate? = null,
    val notas: String? = null,
)

// Campos omitidos (null) ficam como estão — por isso não há forma de limpar uma janela por aqui.
// Marcar como consumida deixou de ser um campo: ver CaveConsumirRequest (consome sempre UMA garrafa).
data class CaveBebidaUpdateRequest(
    @field:Positive
    val quantidade: Int? = null,
    val precoPago: BigDecimal? = null,
    val dataAquisicao: LocalDate? = null,
    val janelaInicio: LocalDate? = null,
    val janelaFim: LocalDate? = null,
    val notas: String? = null,
)

// Corpo opcional de POST /api/caves/{id}/bebidas/{caveBebidaId}/consumir.
data class CaveConsumirRequest(
    // Por omissão, hoje. Não pode ser no futuro.
    val dataConsumo: LocalDate? = null,
    val notas: String? = null,
)

// `restantes`: garrafas que ainda ficam na linha original (0 = a linha deixou de estar na cave).
// `consumida`: a linha que ficou como histórico.
data class CaveConsumoResponse(
    val restantes: Int,
    val consumida: CaveBebidaResponse,
)

data class CaveBebidaResponse(
    val id: Long,
    val bebidaId: Long?,
    val bebidaNome: String?,
    val categoriaNome: String?,
    val imagePath: String?,
    val quantidade: Int,
    val precoPago: BigDecimal?,
    val dataAquisicao: LocalDate?,
    val janelaInicio: LocalDate?,
    val janelaFim: LocalDate?,
    // EM_GUARDA | PRONTA | EM_ATRASO (mostrar o aviso vermelho "Em atraso") | CONSUMIDA
    val estado: EstadoCaveBebida,
    val isConsumida: Boolean,
    val dataConsumo: LocalDate?,
    val notas: String?,
) {
    companion object {
        // `bebida` e `bebida.categoria` são LAZY: ou vêm carregadas (CaveBebidaRepository.findAtivasByCaveId) ou
        // isto corre dentro de uma transação (CaveService).
        fun from(caveBebida: CaveBebida, hoje: LocalDate) = CaveBebidaResponse(
            id = caveBebida.id,
            bebidaId = caveBebida.bebida?.id,
            bebidaNome = caveBebida.bebida?.nome,
            categoriaNome = caveBebida.bebida?.categoria?.value,
            imagePath = caveBebida.bebida?.pathImage,
            quantidade = caveBebida.quantidade,
            precoPago = caveBebida.precoPago,
            dataAquisicao = caveBebida.dataAquisicao,
            janelaInicio = caveBebida.janelaInicio,
            janelaFim = caveBebida.janelaFim,
            estado = EstadoCaveBebida.de(caveBebida, hoje),
            isConsumida = caveBebida.isConsumida,
            dataConsumo = caveBebida.dataConsumo,
            notas = caveBebida.notas,
        )
    }
}
