package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.CaveBebidaRequest
import pt.aquavitae.android.data.model.CaveBebidaResponse
import pt.aquavitae.android.data.model.CaveBebidaUpdateRequest
import pt.aquavitae.android.data.model.CaveDetailResponse
import pt.aquavitae.android.data.model.CaveRequest
import pt.aquavitae.android.data.model.CaveResponse
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/**
 * Repositório da Cave Virtual: o utilizador pode ter várias caves, cada uma com
 * garrafas (cave_bebida) que registam quantidade, preço pago, data de aquisição
 * e, opcionalmente, consumo.
 */
class CaveRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun getCaves(): Result<List<CaveResponse>> = runCatching {
        api.getCaves()
    }

    suspend fun createCave(nome: String, descricao: String?): Result<CaveResponse> = runCatching {
        api.createCave(CaveRequest(nome = nome, descricao = descricao))
    }

    suspend fun getCaveDetail(caveId: Long): Result<CaveDetailResponse> = runCatching {
        api.getCaveDetail(caveId)
    }

    suspend fun addBebidaToCave(
        caveId: Long,
        bebidaId: Long,
        quantidade: Int,
        precoPago: Double?,
        dataAquisicao: String?,
    ): Result<CaveBebidaResponse> = runCatching {
        api.addBebidaToCave(
            caveId,
            CaveBebidaRequest(
                bebidaId = bebidaId,
                quantidade = quantidade,
                precoPago = precoPago,
                dataAquisicao = dataAquisicao,
            ),
        )
    }

    suspend fun updateCaveBebida(
        caveId: Long,
        caveBebidaId: Long,
        isConsumida: Boolean? = null,
        dataConsumo: String? = null,
        notas: String? = null,
    ): Result<CaveBebidaResponse> = runCatching {
        api.updateCaveBebida(
            caveId,
            caveBebidaId,
            CaveBebidaUpdateRequest(isConsumida = isConsumida, dataConsumo = dataConsumo, notas = notas),
        )
    }

    suspend fun removeBebidaFromCave(caveId: Long, caveBebidaId: Long): Result<Unit> = runCatching {
        api.removeBebidaFromCave(caveId, caveBebidaId)
    }
}
