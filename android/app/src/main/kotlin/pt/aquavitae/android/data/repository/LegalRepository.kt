package pt.aquavitae.android.data.repository

import pt.aquavitae.android.data.model.TermosTexto
import pt.aquavitae.android.data.network.AquaVitaeApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * O texto dos termos e condições (vem da API). Guarda-se em memória depois do 1.º pedido, por isso abrir os termos outra vez
 * (no login, no registo, na recuperação) é instantâneo; ao reabrir a app volta a pedir-se, e assim apanha-se um texto novo.
 */
@Singleton
class LegalRepository @Inject constructor(
    private val api: AquaVitaeApi,
) {
    @Volatile
    private var guardado: TermosTexto? = null

    suspend fun termos(): Result<TermosTexto> {
        guardado?.let { return Result.success(it) }
        return runCatching { api.getTermos() }.onSuccess { guardado = it }
    }
}
