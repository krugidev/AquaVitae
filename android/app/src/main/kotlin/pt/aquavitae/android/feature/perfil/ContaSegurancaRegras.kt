package pt.aquavitae.android.feature.perfil

import pt.aquavitae.android.data.model.UtilizadorMe

private fun plural(n: Int, singular: String, plural: String) = "$n ${if (n == 1) singular else plural}"

/**
 * O que o popup "Apagar conta" diz que se perde, a partir dos totais do perfil: só entram as coisas que existem ("2 caves (5
 * garrafas)", "1 review"). Sem nada guardado, devolve uma lista vazia (o popup diz então que só se apaga a conta).
 */
fun resumoDoQueSePerde(utilizador: UtilizadorMe?): List<String> {
    if (utilizador == null) return emptyList()
    return buildList {
        if (utilizador.totalCaves > 0) {
            val garrafas = if (utilizador.totalGarrafas > 0) " (${plural(utilizador.totalGarrafas, "garrafa", "garrafas")})" else ""
            add(plural(utilizador.totalCaves, "cave", "caves") + garrafas)
        }
        if (utilizador.totalFavoritos > 0) add(plural(utilizador.totalFavoritos, "favorito", "favoritos"))
        if (utilizador.totalWishlist > 0) add("${utilizador.totalWishlist} na wishlist")
        if (utilizador.totalProvadas > 0) add(plural(utilizador.totalProvadas, "bebida provada", "bebidas provadas"))
        if (utilizador.totalReviews > 0) add(plural(utilizador.totalReviews, "review", "reviews"))
    }
}
