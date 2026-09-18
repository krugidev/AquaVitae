package pt.aquavitae.api.utilizador

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
) {

    fun getMe(utilizadorId: Long): UtilizadorMeDto = buildMeDto(findWithProfile(utilizadorId))

    @Transactional
    fun updateProfile(utilizadorId: Long, request: UtilizadorUpdateRequest): UtilizadorMeDto {
        val utilizador = findWithProfile(utilizadorId)

        request.username?.let { novoUsername ->
            if (novoUsername != utilizador.username && utilizadorRepository.existsByUsername(novoUsername)) {
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
    )
}
