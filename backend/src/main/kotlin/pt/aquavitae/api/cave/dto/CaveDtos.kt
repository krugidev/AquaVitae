package pt.aquavitae.api.cave.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import pt.aquavitae.api.cave.Cave
import pt.aquavitae.api.cave.CaveBebida
import java.time.LocalDate

data class CaveRequest(
    @field:NotBlank
    val nome: String,
    val descricao: String? = null,
)

data class CaveResponse(
    val id: Long,
    val nome: String?,
    val descricao: String?,
) {
    companion object {
        fun from(cave: Cave) = CaveResponse(id = cave.id, nome = cave.nome, descricao = cave.descricao)
    }
}

data class CaveDetailResponse(
    val id: Long,
    val nome: String?,
    val descricao: String?,
    val bebidas: List<CaveBebidaResponse>,
)

data class CaveBebidaRequest(
    @field:NotNull
    val bebidaId: Long,
    @field:Positive
    val quantidade: Int = 1,
    val precoPago: Double? = null,
    val dataAquisicao: LocalDate? = null,
)

data class CaveBebidaUpdateRequest(
    val isConsumida: Boolean? = null,
    val dataConsumo: LocalDate? = null,
    val notas: String? = null,
)

data class CaveBebidaResponse(
    val id: Long,
    val bebidaId: Long?,
    val bebidaNome: String?,
    val quantidade: Int,
    val precoPago: Double?,
    val dataAquisicao: LocalDate?,
    val isConsumida: Boolean,
    val dataConsumo: LocalDate?,
    val notas: String?,
) {
    companion object {
        fun from(caveBebida: CaveBebida) = CaveBebidaResponse(
            id = caveBebida.id,
            bebidaId = caveBebida.bebida?.id,
            bebidaNome = caveBebida.bebida?.nome,
            quantidade = caveBebida.quantidade,
            precoPago = caveBebida.precoPago,
            dataAquisicao = caveBebida.dataAquisicao,
            isConsumida = caveBebida.isConsumida,
            dataConsumo = caveBebida.dataConsumo,
            notas = caveBebida.notas,
        )
    }
}
