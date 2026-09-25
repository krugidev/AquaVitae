package pt.aquavitae.android.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Abre um URL fora da app (o browser, ou a app da loja se a tiver): usado pelo site do produtor, pelo "Abrir no mapa" e
 * pelos botões de comprar. Devolve `false` se não há nada que o abra (sem browser) ou o URL é inválido — quem chama decide
 * se avisa; nunca rebenta.
 */
fun Context.abrirLink(url: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}
