package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.CaveBebidaRequest
import pt.aquavitae.android.data.model.CaveBebidaResponse
import pt.aquavitae.android.data.model.CaveBebidaUpdateRequest
import pt.aquavitae.android.data.model.CaveConsumirRequest
import pt.aquavitae.android.data.model.CaveConsumoResponse
import pt.aquavitae.android.data.model.CaveDetailResponse
import pt.aquavitae.android.data.model.CaveRequest
import pt.aquavitae.android.data.model.CaveResponse
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/**
 * Repositório da Cave Virtual: o utilizador pode ter várias caves, cada uma com garrafas (`cave_bebida`) que registam
 * quantidade, preço pago, data de aquisição, janela de consumo e, opcionalmente, consumo.
 */
class CaveRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    /** `bebidaId`: destaca (`temBebida`) as caves onde essa bebida já está — o popup "Adicionar à cave". */
    suspend fun getCaves(bebidaId: Long? = null): Result<List<CaveResponse>> = runCatching {
        api.getCaves(bebidaId)
    }

    suspend fun createCave(nome: String, descricao: String?): Result<CaveResponse> = runCatching {
        api.createCave(CaveRequest(nome = nome, descricao = descricao))
    }

    suspend fun getCaveDetail(caveId: Long, sort: String? = null): Result<CaveDetailResponse> = runCatching {
        api.getCaveDetail(caveId, sort)
    }

    suspend fun addBebidaToCave(
        caveId: Long,
        bebidaId: Long,
        quantidade: Int,
        precoPago: Double?,
        dataAquisicao: String?,
        janelaInicio: String? = null,
        janelaFim: String? = null,
        notas: String? = null,
    ): Result<CaveBebidaResponse> = runCatching {
        api.addBebidaToCave(
            caveId,
            CaveBebidaRequest(
                bebidaId = bebidaId,
                quantidade = quantidade,
                precoPago = precoPago,
                dataAquisicao = dataAquisicao,
                janelaInicio = janelaInicio,
                janelaFim = janelaFim,
                notas = notas,
            ),
        )
    }

    suspend fun updateCaveBebida(
        caveId: Long,
        caveBebidaId: Long,
        quantidade: Int? = null,
        precoPago: Double? = null,
        dataAquisicao: String? = null,
        janelaInicio: String? = null,
        janelaFim: String? = null,
        notas: String? = null,
    ): Result<CaveBebidaResponse> = runCatching {
        api.updateCaveBebida(
            caveId,
            caveBebidaId,
            CaveBebidaUpdateRequest(
                quantidade = quantidade,
                precoPago = precoPago,
                dataAquisicao = dataAquisicao,
                janelaInicio = janelaInicio,
                janelaFim = janelaFim,
                notas = notas,
            ),
        )
    }

    suspend fun removeBebidaFromCave(caveId: Long, caveBebidaId: Long): Result<Unit> = runCatching {
        api.removeBebidaFromCave(caveId, caveBebidaId)
    }

    suspend fun consumir(caveId: Long, caveBebidaId: Long, dataConsumo: String? = null, notas: String? = null): Result<CaveConsumoResponse> =
        runCatching { api.consumirBebida(caveId, caveBebidaId, CaveConsumirRequest(dataConsumo = dataConsumo, notas = notas)) }
}
