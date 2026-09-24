package pt.aquavitae.android.navigation

/**
 * Rotas do grafo de navegação (Navigation Compose), na ordem do fluxo MVP:
 * Loading -> Login/Registo -> Onboarding -> Catálogo -> Detalhe -> Reviews -> Cave/Wishlist/Favoritos.
 */
object AppDestinations {
    const val ARG_BEBIDA_ID = "bebidaId"

    /** Chave que a recuperação de password deixa no `savedStateHandle` do login ao concluir (ver `AppNavHost`). */
    const val KEY_PASSWORD_CHANGED = "passwordChanged"

    const val LOADING = "loading"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RECOVER = "recover"
    const val ONBOARDING = "onboarding"
    /** A homepage: ecrã de destino depois do login (o botão "Home", ao centro da barra de navegação). */
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val DETAIL_ROUTE = "detail/{$ARG_BEBIDA_ID}"
    const val REVIEWS_ROUTE = "reviews/{$ARG_BEBIDA_ID}"
    const val CAVE = "cave"
    /** "Já provadas": alcança-se a partir da Cave ("Carregar mais antigas" da secção) ou do perfil ("Histórico de provadas"). */
    const val PROVADAS = "provadas"
    const val WISHLIST = "wishlist"
    const val FAVORITOS = "favoritos"
    /** Perfil (fatia 5): alcança-se tocando no avatar em qualquer um dos 3 cabeçalhos que o mostram. */
    const val PERFIL = "perfil"
    const val PERFIL_EDITAR = "perfil/editar"

    fun detail(bebidaId: Long) = "detail/$bebidaId"
    fun reviews(bebidaId: Long) = "reviews/$bebidaId"
}
