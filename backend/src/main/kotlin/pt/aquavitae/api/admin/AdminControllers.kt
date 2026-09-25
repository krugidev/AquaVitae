package pt.aquavitae.api.admin

import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

private const val TEXTO_MAXIMO = 100

// Uma pesquisa por texto livre: sem os espaços das pontas e com um tamanho razoável (a pesquisa vai para um LIKE).
private fun textoDaPesquisa(q: String): String = q.trim().take(TEXTO_MAXIMO)

@Controller
class AdminController(
    private val catalogo: AdminCatalogoService,
) {

    // O formulário de login é processado pelo Spring Security (POST no mesmo URL, ver AdminSecurityConfig).
    @GetMapping(ADMIN_LOGIN)
    fun login(): String = "admin/login"

    @GetMapping("/admin")
    fun painel(model: Model): String {
        model.addAttribute("resumo", catalogo.resumo())
        return "admin/painel"
    }
}

/**
 * A lista de bebidas: pesquisa (sem acentos), categoria, critério de qualidade e ordenação, paginada. Devolve a página inteira;
 * a um pedido do htmx (a pesquisa enquanto se escreve) devolve só o bloco `resultado`, que ele troca no sítio.
 */
@Controller
@RequestMapping("/admin/bebidas")
class AdminBebidaController(
    private val catalogo: AdminCatalogoService,
) {

    @GetMapping
    fun listar(
        @RequestParam(name = "q", defaultValue = "") q: String,
        @RequestParam(name = "categoria", required = false) categoria: Long?,
        @RequestParam(name = "qualidade", defaultValue = "") qualidade: String,
        @RequestParam(name = "ordem", defaultValue = "") ordem: String,
        @RequestParam(name = "pagina", defaultValue = "0") pagina: Int,
        @RequestHeader(name = "HX-Request", required = false) htmx: String?,
        @RequestHeader(name = "HX-History-Restore-Request", required = false) restauro: String?,
        model: Model,
        response: HttpServletResponse,
    ): String {
        val texto = textoDaPesquisa(q)
        val filtroQualidade = QualidadeBebida.deCodigo(qualidade)
        val filtroOrdem = OrdemBebidas.deCodigo(ordem)

        model.addAttribute("q", texto)
        model.addAttribute("categoriaId", categoria)
        model.addAttribute("qualidade", filtroQualidade?.codigo.orEmpty())
        model.addAttribute("ordem", filtroOrdem.codigo)
        model.addAttribute("resultados", catalogo.listarBebidas(texto, categoria, filtroQualidade, filtroOrdem, pagina))
        return paginaOuBloco(htmx, restauro, "admin/bebidas", response) {
            model.addAttribute("categorias", catalogo.categorias())
            model.addAttribute("qualidades", QualidadeBebida.entries)
            model.addAttribute("ordens", OrdemBebidas.entries)
        }
    }
}

@Controller
@RequestMapping("/admin/produtores")
class AdminProdutorController(
    private val catalogo: AdminCatalogoService,
) {

    @GetMapping
    fun listar(
        @RequestParam(name = "q", defaultValue = "") q: String,
        @RequestParam(name = "qualidade", defaultValue = "") qualidade: String,
        @RequestParam(name = "ordem", defaultValue = "") ordem: String,
        @RequestParam(name = "pagina", defaultValue = "0") pagina: Int,
        @RequestHeader(name = "HX-Request", required = false) htmx: String?,
        @RequestHeader(name = "HX-History-Restore-Request", required = false) restauro: String?,
        model: Model,
        response: HttpServletResponse,
    ): String {
        val texto = textoDaPesquisa(q)
        val filtroQualidade = QualidadeProdutor.deCodigo(qualidade)
        val filtroOrdem = OrdemProdutores.deCodigo(ordem)

        model.addAttribute("q", texto)
        model.addAttribute("qualidade", filtroQualidade?.codigo.orEmpty())
        model.addAttribute("ordem", filtroOrdem.codigo)
        model.addAttribute("resultados", catalogo.listarProdutores(texto, filtroQualidade, filtroOrdem, pagina))
        return paginaOuBloco(htmx, restauro, "admin/produtores", response) {
            model.addAttribute("qualidades", QualidadeProdutor.entries)
            model.addAttribute("ordens", OrdemProdutores.entries)
        }
    }
}

// Um pedido do htmx só precisa do bloco `resultado` (o formulário de filtros já está na página); um pedido normal, da página inteira
// (e dos dados dos filtros, que só ela desenha). O `Vary` evita que o browser/histórico sirvam o bloco onde devia estar a página.
private fun paginaOuBloco(htmx: String?, restauro: String?, vista: String, response: HttpServletResponse, dadosDaPagina: () -> Unit): String {
    response.addHeader("Vary", "HX-Request")
    if (htmx != null && restauro == null) return "$vista :: resultado"
    dadosDaPagina()
    return vista
}
