package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject

/**
 * A lista "Já provadas" e marcar/desmarcar uma bebida como provada — pré-requisito para lhe escrever uma review (o
 * backend devolve 409 sem isto). `addProvada` é idempotente do lado do backend (chamar duas vezes não duplica nem dá
 * erro) — os dois caminhos para lá entrar são "Consumir" numa cave ou adicionar diretamente nesta lista.
 */
class ProvadaRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    suspend fun getProvadas(categoriaId: Long? = null, ano: Int? = null): Result<List<BebidaRelacao>> =
        runCatching { api.getProvadas(categoriaId, ano) }

    suspend fun addProvada(bebidaId: Long): Result<Unit> = runCatching { api.addProvada(bebidaId) }

    suspend fun removeProvada(bebidaId: Long): Result<Unit> = runCatching { api.removeProvada(bebidaId) }
}
