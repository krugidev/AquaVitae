package pt.aquavitae.api.lookup

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.lookup.dto.AvatarDto
import pt.aquavitae.api.lookup.dto.CastaDto
import pt.aquavitae.api.lookup.dto.LookupItemDto
import pt.aquavitae.api.lookup.dto.toLookupItemDto
import pt.aquavitae.api.produtor.ProdutorRepository

// Endpoints de leitura para as tabelas de lookup geridas pelo admin (ver
// briefing secção 4 — a API só lê, nunca escreve aqui). Sem auth: são dados
// públicos usados em formulários (registo, filtros, preferências).
@RestController
@RequestMapping("/api/lookup")
class LookupController(
    private val nationalityRepository: UtilizadorNationalityRepository,
    private val avatarCategoriaRepository: AvatarCategoriaRepository,
    private val avatarRepository: UtilizadorAvatarRepository,
    private val bebidaCategoriaRepository: BebidaCategoriaRepository,
    private val castaRepository: CastaRepository,
    private val castaTipoRepository: CastaTipoRepository,
    private val paisRepository: PaisRepository,
    private val produtorRepository: ProdutorRepository,
    private val vinhoCorpoRepository: VinhoCorpoRepository,
    private val vinhoTaninoRepository: VinhoTaninoRepository,
    private val vinhoTipoRepository: VinhoTipoRepository,
) {

    @GetMapping("/nacionalidades")
    fun nacionalidades(): List<LookupItemDto> =
        nationalityRepository.findAll().map { LookupItemDto(it.id, it.value) }

    @GetMapping("/avatar-categorias")
    fun avatarCategorias(): List<LookupItemDto> =
        avatarCategoriaRepository.findAll().map { it.toLookupItemDto() }

    @GetMapping("/avatares")
    fun avatares(@RequestParam categoriaId: Long?): List<AvatarDto> {
        val avatares = if (categoriaId != null) {
            avatarRepository.findByCategoria_IdAndIsActiveTrue(categoriaId)
        } else {
            avatarRepository.findByIsActiveTrue()
        }
        return avatares.map { AvatarDto.from(it) }
    }

    @GetMapping("/categorias-bebida")
    fun categoriasBebida(): List<LookupItemDto> =
        bebidaCategoriaRepository.findAll().map { LookupItemDto(it.id, it.value) }

    // ~280 castas: por ordem alfabética (o Oracle ordenaria por código de carácter, ver OrdemAlfabetica.kt).
    @GetMapping("/castas")
    fun castas(@RequestParam tipoId: Long?): List<CastaDto> {
        val castas = if (tipoId != null) castaRepository.findByTipo_Id(tipoId) else castaRepository.findAll()
        return castas.map { CastaDto.from(it) }.ordenadoPorNome { it.nome }
    }

    @GetMapping("/casta-tipos")
    fun castaTipos(): List<LookupItemDto> =
        castaTipoRepository.findAll().map { it.toLookupItemDto() }

    // ~218 países: por ordem alfabética (ver OrdemAlfabetica.kt).
    @GetMapping("/paises")
    fun paises(): List<LookupItemDto> =
        paisRepository.findAll().map { LookupItemDto(it.id, it.value) }.ordenadoPorNome { it.nome }

    // paisId aqui refere-se a produtor_pais_id (regiões são um atributo do produtor, não da bebida — ver
    // nota em ProdutorRepository.findDistinctRegioesByPaisId). pais e produtor_pais são duas tabelas mas
    // com os MESMOS ids (convenção do seed, ver database/README.md), por isso é o mesmo id que
    // GET /api/bebidas?paisId= e /lookup/paises devolvem.
    @GetMapping("/regioes")
    fun regioes(@RequestParam paisId: Long): List<String> =
        produtorRepository.findDistinctRegioesByPaisId(paisId).ordenadoPorNome { it }

    @GetMapping("/vinho/corpos")
    fun vinhoCorpos(): List<LookupItemDto> =
        vinhoCorpoRepository.findAll().map { LookupItemDto(it.id, it.value) }

    @GetMapping("/vinho/taninos")
    fun vinhoTaninos(): List<LookupItemDto> =
        vinhoTaninoRepository.findAll().map { LookupItemDto(it.id, it.value) }

    @GetMapping("/vinho/tipos")
    fun vinhoTipos(): List<LookupItemDto> =
        vinhoTipoRepository.findAll().map { LookupItemDto(it.id, it.value) }
}
