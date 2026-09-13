package pt.aquavitae.android.navigation

/**
 * Rotas do grafo de navegação (Navigation Compose), na ordem do fluxo MVP:
 * Login/Registo -> Onboarding -> Catálogo -> Detalhe -> Reviews -> Cave/Wishlist/Favoritos.
 */
object AppDestinations {
    const val ARG_BEBIDA_ID = "bebidaId"

    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ONBOARDING = "onboarding"
    const val CATALOG = "catalog"
    const val DETAIL_ROUTE = "detail/{$ARG_BEBIDA_ID}"
    const val REVIEWS_ROUTE = "reviews/{$ARG_BEBIDA_ID}"
    const val CAVE = "cave"
    const val WISHLIST = "wishlist"
    const val FAVORITOS = "favoritos"

    fun detail(bebidaId: Long) = "detail/$bebidaId"
    fun reviews(bebidaId: Long) = "reviews/$bebidaId"
}
