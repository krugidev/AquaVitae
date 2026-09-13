package pt.aquavitae.api.cave

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.bebida.BebidaRepository
import pt.aquavitae.api.cave.dto.CaveBebidaRequest
import pt.aquavitae.api.cave.dto.CaveBebidaResponse
import pt.aquavitae.api.cave.dto.CaveBebidaUpdateRequest
import pt.aquavitae.api.cave.dto.CaveDetailResponse
import pt.aquavitae.api.cave.dto.CaveRequest
import pt.aquavitae.api.cave.dto.CaveResponse
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.common.UnauthorizedActionException
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Instant

@Service
class CaveService(
    private val caveRepository: CaveRepository,
    private val caveBebidaRepository: CaveBebidaRepository,
    private val bebidaRepository: BebidaRepository,
) {

    fun listByUtilizador(utilizador: Utilizador): List<CaveResponse> =
        caveRepository.findByUtilizador_Id(utilizador.id).map { CaveResponse.from(it) }

    @Transactional
    fun create(utilizador: Utilizador, request: CaveRequest): CaveResponse {
        val cave = Cave(
            utilizador = utilizador,
            nome = request.nome,
            descricao = request.descricao,
            dataCriacao = Instant.now(),
        )
        return CaveResponse.from(caveRepository.save(cave))
    }

    fun getDetail(caveId: Long, utilizador: Utilizador): CaveDetailResponse {
        val cave = findOwned(caveId, utilizador)
        val bebidas = caveBebidaRepository.findByCave_Id(cave.id).map { CaveBebidaResponse.from(it) }
        return CaveDetailResponse(id = cave.id, nome = cave.nome, descricao = cave.descricao, bebidas = bebidas)
    }

    @Transactional
    fun addBebida(caveId: Long, utilizador: Utilizador, request: CaveBebidaRequest): CaveBebidaResponse {
        val cave = findOwned(caveId, utilizador)
        val bebida = bebidaRepository.findById(request.bebidaId)
            .orElseThrow { ResourceNotFoundException("Bebida ${request.bebidaId} não encontrada") }

        val caveBebida = CaveBebida(
            cave = cave,
            bebida = bebida,
            quantidade = request.quantidade,
            precoPago = request.precoPago,
            dataAquisicao = request.dataAquisicao,
            dataCriacao = Instant.now(),
        )
        return CaveBebidaResponse.from(caveBebidaRepository.save(caveBebida))
    }

    @Transactional
    fun updateBebida(caveId: Long, caveBebidaId: Long, utilizador: Utilizador, request: CaveBebidaUpdateRequest): CaveBebidaResponse {
        findOwned(caveId, utilizador)
        val caveBebida = caveBebidaRepository.findByIdAndCave_Id(caveBebidaId, caveId)
            ?: throw ResourceNotFoundException("Item $caveBebidaId não encontrado nesta cave")

        request.isConsumida?.let { caveBebida.isConsumida = it }
        request.dataConsumo?.let { caveBebida.dataConsumo = it }
        request.notas?.let { caveBebida.notas = it }

        return CaveBebidaResponse.from(caveBebidaRepository.save(caveBebida))
    }

    @Transactional
    fun removeBebida(caveId: Long, caveBebidaId: Long, utilizador: Utilizador) {
        findOwned(caveId, utilizador)
        val caveBebida = caveBebidaRepository.findByIdAndCave_Id(caveBebidaId, caveId)
            ?: throw ResourceNotFoundException("Item $caveBebidaId não encontrado nesta cave")
        caveBebidaRepository.delete(caveBebida)
    }

    private fun findOwned(caveId: Long, utilizador: Utilizador): Cave {
        val cave = caveRepository.findById(caveId)
            .orElseThrow { ResourceNotFoundException("Cave $caveId não encontrada") }
        if (cave.utilizador?.id != utilizador.id) {
            throw UnauthorizedActionException("Esta cave não pertence a este utilizador")
        }
        return cave
    }
}
