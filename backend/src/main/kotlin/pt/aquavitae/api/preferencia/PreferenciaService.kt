package pt.aquavitae.api.preferencia

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.lookup.BebidaCategoriaRepository
import pt.aquavitae.api.lookup.CastaRepository
import pt.aquavitae.api.preferencia.dto.PreferenciaRequest
import pt.aquavitae.api.preferencia.dto.PreferenciaResponse
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant

@Service
class PreferenciaService(
    private val preferenciaRepository: UtilizadorPreferenciaRepository,
    private val categoriaPreferidaRepository: UtilizadorCategoriaPreferidaRepository,
    private val castaPreferidaRepository: UtilizadorCastaPreferidaRepository,
    private val bebidaCategoriaRepository: BebidaCategoriaRepository,
    private val castaRepository: CastaRepository,
) {

    @Transactional
    fun upsert(utilizador: Utilizador, request: PreferenciaRequest): PreferenciaResponse {
        val preferencia = preferenciaRepository.findByUtilizador_Id(utilizador.id)
            .orElse(UtilizadorPreferencia(utilizador = utilizador))

        preferencia.acidezMin = request.acidezMin
        preferencia.acidezMax = request.acidezMax
        preferencia.docuraMin = request.docuraMin
        preferencia.docuraMax = request.docuraMax
        preferencia.dataAtualizacao = Instant.now()
        preferenciaRepository.save(preferencia)

        // Estratégia "replace all": mais simples e suficiente para um PUT
        // idempotente de preferências de onboarding.
        categoriaPreferidaRepository.deleteByUtilizador_Id(utilizador.id)
        val categorias = bebidaCategoriaRepository.findAllById(request.categoriaIds)
        categoriaPreferidaRepository.saveAll(
            categorias.map { UtilizadorCategoriaPreferida(utilizador = utilizador, categoria = it) },
        )

        castaPreferidaRepository.deleteByUtilizador_Id(utilizador.id)
        val castas = castaRepository.findAllById(request.castaIds)
        castaPreferidaRepository.saveAll(
            castas.map { UtilizadorCastaPreferida(utilizador = utilizador, casta = it) },
        )

        return PreferenciaResponse(
            acidezMin = preferencia.acidezMin,
            acidezMax = preferencia.acidezMax,
            docuraMin = preferencia.docuraMin,
            docuraMax = preferencia.docuraMax,
            categoriaIds = categorias.map { it.id },
            castaIds = castas.map { it.id },
        )
    }
}
