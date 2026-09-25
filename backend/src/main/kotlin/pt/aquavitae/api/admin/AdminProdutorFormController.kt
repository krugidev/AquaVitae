package pt.aquavitae.api.admin

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

private const val VISTA_FORMULARIO = "admin/produtor-form"

/**
 * Criar e editar um produtor (fatia 2). Os formulários são `POST` normais (o token CSRF vai sozinho com o `th:action`); depois de
 * gravar, redireciona para a página de edição (`?criado` / `?guardado` mostra o aviso), para um F5 não voltar a enviar o formulário.
 * Com erros mostra o formulário outra vez, com o que se escreveu e a mensagem junto de cada campo.
 */
@Controller
@RequestMapping("/admin/produtores")
class AdminProdutorFormController(
    private val servico: AdminProdutorService,
) {

    @GetMapping("/novo")
    fun novo(model: Model): String {
        preparar(model, ProdutorFormulario(), erros = emptyMap())
        return VISTA_FORMULARIO
    }

    @PostMapping
    fun criar(@ModelAttribute("form") form: ProdutorFormulario, model: Model): String =
        when (val resultado = servico.criar(form)) {
            is ResultadoGravacao.Guardado -> "redirect:/admin/produtores/${resultado.id}?criado"
            is ResultadoGravacao.Invalido -> {
                preparar(model, form, resultado.erros)
                VISTA_FORMULARIO
            }
        }

    @GetMapping("/{id}")
    fun editar(@PathVariable id: Long, model: Model): String {
        val produtor = servico.carregar(id)
        preparar(model, produtor.formulario, erros = emptyMap(), produtor = produtor)
        return VISTA_FORMULARIO
    }

    @PostMapping("/{id}")
    fun atualizar(@PathVariable id: Long, @ModelAttribute("form") form: ProdutorFormulario, model: Model): String =
        when (val resultado = servico.atualizar(id, form)) {
            is ResultadoGravacao.Guardado -> "redirect:/admin/produtores/$id?guardado"
            is ResultadoGravacao.Invalido -> {
                preparar(model, form, resultado.erros, produtor = servico.carregar(id))
                VISTA_FORMULARIO
            }
        }

    // As regiões do país escolhido, como `<option>`s: o htmx pede-as quando o país muda e troca o conteúdo do select da região.
    @GetMapping("/regioes")
    fun regioes(@RequestParam(name = "paisId", required = false) paisId: Long?, model: Model): String {
        model.addAttribute("regioes", servico.regioesDoPais(paisId))
        model.addAttribute("form", ProdutorFormulario())
        return "$VISTA_FORMULARIO :: opcoesRegiao"
    }

    private fun preparar(model: Model, form: ProdutorFormulario, erros: Map<String, String>, produtor: ProdutorParaEditar? = null) {
        model.addAttribute("form", form)
        model.addAttribute("erros", erros)
        model.addAttribute("produtor", produtor)
        model.addAttribute("paises", servico.paises())
        model.addAttribute("regioes", servico.regioesDoPais(form.paisId.trim().toLongOrNull()))
    }
}
