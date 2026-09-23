package pt.aquavitae.api.utilizador

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.aquavitae.api.cave.CaveBebidaRepository
import pt.aquavitae.api.cave.CaveRepository
import pt.aquavitae.api.common.ConflictException
import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.favorito.FavoritoRepository
import pt.aquavitae.api.lookup.UtilizadorAvatarRepository
import pt.aquavitae.api.lookup.UtilizadorNationalityRepository
import pt.aquavitae.api.lookup.dto.AvatarDto
import pt.aquavitae.api.provada.BebidaProvadaRepository
import pt.aquavitae.api.review.ReviewRepository
import pt.aquavitae.api.utilizador.dto.UtilizadorMeDto
import pt.aquavitae.api.utilizador.dto.UtilizadorUpdateRequest
import pt.aquavitae.api.wishlist.WishlistRepository
import java.time.Clock
import java.time.Instant

@Service
class UtilizadorService(
    private val utilizadorRepository: UtilizadorRepository,
    private val nationalityRepository: UtilizadorNationalityRepository,
    private val avatarRepository: UtilizadorAvatarRepository,
    private val favoritoRepository: FavoritoRepository,
    private val wishlistRepository: WishlistRepository,
    private val bebidaProvadaRepository: BebidaProvadaRepository,
    private val reviewRepository: ReviewRepository,
    private val caveRepository: CaveRepository,
    private val caveBebidaRepository: CaveBebidaRepository,
    private val clock: Clock,
    // "AAAA-MM-DD" (hora de Portugal) em que os termos em vigor foram publicados; vazio = ainda não mudaram.
    @Value("\${aquavitae.termos.em-vigor-desde:}") termosEmVigorDesdeConfig: String,
) {

    private val termosEmVigorDesde: Instant? = emVigorDesdeInstant(termosEmVigorDesdeConfig, clock.zone)

    fun getMe(utilizadorId: Long): UtilizadorMeDto = buildMeDto(findWithProfile(utilizadorId))

    // Regista a aceitação agora. Também serve para voltar a aceitar depois de os termos mudarem (substitui a data).
    @Transactional
    fun aceitarTermos(utilizadorId: Long): UtilizadorMeDto {
        val utilizador = findWithProfile(utilizadorId)
        utilizador.termosAceitesEm = clock.instant()
        return buildMeDto(utilizadorRepository.save(utilizador))
    }

    @Transactional
    fun updateProfile(utilizadorId: Long, request: UtilizadorUpdateRequest): UtilizadorMeDto {
        val utilizador = findWithProfile(utilizadorId)

        request.username?.let { novoUsername ->
            // Só muda a capitalização do próprio username (ana -> Ana): não é conflito.
            if (!novoUsername.equals(utilizador.username, ignoreCase = true) &&
                utilizadorRepository.existsByUsernameIgnoreCase(novoUsername)
            ) {
                throw ConflictException("O username $novoUsername já está em uso")
            }
            utilizador.username = novoUsername
        }
        request.firstName?.let { utilizador.firstName = it }
        request.lastName?.let { utilizador.lastName = it }
        request.bioDesc?.let { utilizador.bioDesc = it }
        request.nationalityId?.let { id ->
            utilizador.nationality = nationalityRepository.findById(id)
                .orElseThrow { ResourceNotFoundException("Nacionalidade $id não encontrada") }
        }
        request.avatarId?.let { id ->
            utilizador.avatar = avatarRepository.findById(id)
                .orElseThrow { ResourceNotFoundException("Avatar $id não encontrado") }
        }

        return buildMeDto(utilizadorRepository.save(utilizador))
    }

    private fun findWithProfile(utilizadorId: Long): Utilizador =
        utilizadorRepository.findByIdWithProfile(utilizadorId)
            .orElseThrow { ResourceNotFoundException("Utilizador $utilizadorId não encontrado") }

    private fun buildMeDto(utilizador: Utilizador): UtilizadorMeDto = UtilizadorMeDto(
        id = utilizador.id,
        username = utilizador.username,
        email = utilizador.email,
        firstName = utilizador.firstName,
        lastName = utilizador.lastName,
        nationality = utilizador.nationality?.value,
        bioDesc = utilizador.bioDesc,
        avatar = utilizador.avatar?.let { AvatarDto.from(it) },
        accountCreatedAt = utilizador.accountCreatedAt,
        totalProvadas = bebidaProvadaRepository.countByUtilizador_Id(utilizador.id).toInt(),
        totalReviews = reviewRepository.countByUtilizador_Id(utilizador.id).toInt(),
        totalFavoritos = favoritoRepository.countByUtilizador_Id(utilizador.id).toInt(),
        totalWishlist = wishlistRepository.countByUtilizador_Id(utilizador.id).toInt(),
        totalCaves = caveRepository.countByUtilizador_Id(utilizador.id).toInt(),
        totalGarrafas = caveBebidaRepository.sumQuantidadeAtivaByUtilizadorId(utilizador.id),
        termosAceitesEm = utilizador.termosAceitesEm,
        precisaAceitarTermos = precisaAceitarTermos(utilizador.termosAceitesEm, termosEmVigorDesde),
    )
}
