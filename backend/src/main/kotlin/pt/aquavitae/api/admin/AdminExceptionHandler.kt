package pt.aquavitae.api.admin

import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pt.aquavitae.api.common.ResourceNotFoundException

/**
 * Um erro numa página do painel aparece como uma página do painel (não como o JSON da API). Só apanha os controllers deste pacote
 * e tem de vir **antes** do `ApiExceptionHandler` (que apanha tudo), por isso a precedência máxima.
 */
@ControllerAdvice(basePackageClasses = [AdminController::class])
@Order(Ordered.HIGHEST_PRECEDENCE)
class AdminExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun naoEncontrado(ex: ResourceNotFoundException, model: Model, response: HttpServletResponse): String {
        response.status = HttpServletResponse.SC_NOT_FOUND
        model.addAttribute("mensagem", ex.message ?: "Não encontrado.")
        return "admin/erro"
    }
}
