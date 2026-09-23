package pt.aquavitae.api.cave

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.bebida.BebidaRepository
import pt.aquavitae.api.cave.dto.CaveBebidaRequest
import pt.aquavitae.api.cave.dto.CaveBebidaResponse
import pt.aquavitae.api.cave.dto.CaveBebidaUpdateRequest
import pt.aquavitae.api.cave.dto.CaveConsumirRequest
import pt.aquavitae.api.cave.dto.CaveConsumoResponse
import pt.aquavitae.api.cave.dto.CaveDetailResponse
import pt.aquavitae.api.cave.dto.CaveRequest
import pt.aquavitae.api.cave.dto.CaveResponse
import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.PedidoInvalidoException
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.common.UnauthorizedActionException
import pt.aquavitae.api.utilizador.Utilizador
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

@Service
class CaveService(
    private val caveRepository: CaveRepository,
    private val caveBebidaRepository: CaveBebidaRepository,
    private val bebidaRepository: BebidaRepository,
    private val clock: Clock,
) {

    // `bebidaId`: para o popup "Adicionar à cave" destacar em que caves aquela bebida já está (`temBebida`) — a
    // mesma query em lote de sempre, só a verificar se alguma linha ativa da cave é dessa bebida.
    @Transactional(readOnly = true)
    fun listByUtilizador(utilizador: Utilizador, bebidaId: Long? = null): List<CaveResponse> {
        val hoje = LocalDate.now(clock)
        val ativasPorCave = caveBebidaRepository.findAtivasByUtilizadorId(utilizador.id)
            .groupBy { it.cave?.id }
        return caveRepository.findByUtilizador_IdOrderByDataCriacaoAscIdAsc(utilizador.id).map { cave ->
            val linhas = ativasPorCave[cave.id].orEmpty()
            val temBebida = bebidaId != null && linhas.any { it.bebida?.id == bebidaId }
            CaveResponse.from(cave, CaveRegras.resumir(linhas, hoje), temBebida)
        }
    }

    @Transactional
    fun create(utilizador: Utilizador, request: CaveRequest): CaveResponse {
        val cave = Cave(
            utilizador = utilizador,
            nome = request.nome,
            descricao = request.descricao,
            dataCriacao = Instant.now(),
        )
        return CaveResponse.from(caveRepository.save(cave), CaveRegras.resumir(emptyList(), LocalDate.now(clock)))
    }

    @Transactional(readOnly = true)
    fun getDetail(caveId: Long, utilizador: Utilizador, sort: String?): CaveDetailResponse {
        val ordenacao = CaveOrdenacao.de(sort)
        val cave = findOwned(caveId, utilizador)
        val hoje = LocalDate.now(clock)

        val ativas = caveBebidaRepository.findAtivasByCaveId(cave.id)
        val (emGuarda, prontas) = ativas.partition { EstadoCaveBebida.de(it, hoje) == EstadoCaveBebida.EM_GUARDA }
        val resumo = CaveRegras.resumir(ativas, hoje)

        return CaveDetailResponse(
            id = cave.id,
            nome = cave.nome,
            descricao = cave.descricao,
            totalGarrafas = resumo.totalGarrafas,
            valorTotal = resumo.valorTotal,
            totalProntasAAbrir = resumo.totalProntasAAbrir,
            prontasAAbrir = CaveRegras.ordenarProntas(prontas, ordenacao).map { CaveBebidaResponse.from(it, hoje) },
            emGuarda = CaveRegras.ordenarEmGuarda(emGuarda, ordenacao).map { CaveBebidaResponse.from(it, hoje) },
        )
    }

    @Transactional
    fun addBebida(caveId: Long, utilizador: Utilizador, request: CaveBebidaRequest): CaveBebidaResponse {
        val cave = findOwned(caveId, utilizador)
        val bebida = bebidaRepository.findById(request.bebidaId)
            .orElseThrow { ResourceNotFoundException("Bebida ${request.bebidaId} não encontrada") }
        CaveRegras.validarJanela(request.janelaInicio, request.janelaFim)

        val caveBebida = CaveBebida(
            cave = cave,
            bebida = bebida,
            quantidade = request.quantidade,
            precoPago = request.precoPago,
            dataAquisicao = request.dataAquisicao,
            janelaInicio = request.janelaInicio,
            janelaFim = request.janelaFim,
            notas = request.notas,
            dataCriacao = Instant.now(),
        )
        return CaveBebidaResponse.from(caveBebidaRepository.save(caveBebida), LocalDate.now(clock))
    }

    @Transactional
    fun updateBebida(caveId: Long, caveBebidaId: Long, utilizador: Utilizador, request: CaveBebidaUpdateRequest): CaveBebidaResponse {
        findOwned(caveId, utilizador)
        val caveBebida = caveBebidaRepository.findByIdAndCave_Id(caveBebidaId, caveId)
            ?: throw ResourceNotFoundException("Item $caveBebidaId não encontrado nesta cave")
        // Uma garrafa consumida é histórico: não se edita (as notas do consumo vão no próprio pedido de consumo).
        if (caveBebida.isConsumida) throw ConflictException("Esta garrafa já foi consumida e não se pode editar")

        request.quantidade?.let { caveBebida.quantidade = it }
        request.precoPago?.let { caveBebida.precoPago = it }
        request.dataAquisicao?.let { caveBebida.dataAquisicao = it }
        request.janelaInicio?.let { caveBebida.janelaInicio = it }
        request.janelaFim?.let { caveBebida.janelaFim = it }
        request.notas?.let { caveBebida.notas = it }
        // Depois de aplicar as alterações: o início pode ter passado a depois do fim que já lá estava.
        CaveRegras.validarJanela(caveBebida.janelaInicio, caveBebida.janelaFim)

        return CaveBebidaResponse.from(caveBebidaRepository.save(caveBebida), LocalDate.now(clock))
    }

    // "Marcar como consumida": consome UMA garrafa (ver CaveRegras.consumirUma).
    @Transactional
    fun consumir(caveId: Long, caveBebidaId: Long, utilizador: Utilizador, request: CaveConsumirRequest?): CaveConsumoResponse {
        findOwned(caveId, utilizador)
        val hoje = LocalDate.now(clock)
        val dataConsumo = request?.dataConsumo ?: hoje
        if (dataConsumo.isAfter(hoje)) throw PedidoInvalidoException("A data de consumo não pode ser no futuro")

        val linha = caveBebidaRepository.findByIdAndCaveIdParaAtualizar(caveBebidaId, caveId)
            ?: throw ResourceNotFoundException("Item $caveBebidaId não encontrado nesta cave")

        val consumo = CaveRegras.consumirUma(linha, dataConsumo, request?.notas, Instant.now())
        caveBebidaRepository.save(linha)
        val consumida = if (consumo.consumida === linha) linha else caveBebidaRepository.save(consumo.consumida)

        return CaveConsumoResponse(restantes = consumo.restantes, consumida = CaveBebidaResponse.from(consumida, hoje))
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
