package pt.aquavitae.api.produtor

import pt.aquavitae.api.common.ResourceNotFoundException
import pt.aquavitae.api.produtor.dto.ProdutorDetailDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/produtores")
class ProdutorController(
    private val produtorRepository: ProdutorRepository,
) {
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ProdutorDetailDto {
        val produtor = produtorRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Produtor $id não encontrado") }
        return ProdutorDetailDto.from(produtor)
    }
}
