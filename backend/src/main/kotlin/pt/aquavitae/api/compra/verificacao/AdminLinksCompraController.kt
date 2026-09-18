package pt.aquavitae.api.compra.verificacao

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.aquavitae.api.common.ConflictException

// Operação de manutenção (só ROLE_ADMIN, ver SecurityConfig): não escreve conteúdo do catálogo,
// só dispara a verificação que o agendamento diário já faz — útil logo a seguir a um lote de seed,
// para não deixar links errados como "disponíveis" até às 04:00.
@RestController
@RequestMapping("/api/admin/links-compra")
class AdminLinksCompraController(
    private val service: LinkVerificacaoService,
) {

    @PostMapping("/verificar")
    fun verificar(): ResponseEntity<EstadoVerificacao> {
        if (!service.iniciarEmBackground()) {
            throw ConflictException("Já existe uma verificação de links em curso")
        }
        return ResponseEntity.accepted().body(service.estado())
    }

    @GetMapping("/verificacao")
    fun estado(): EstadoVerificacao = service.estado()
}
