package pt.aquavitae.android.data.network

import pt.aquavitae.android.BuildConfig

/**
 * O URL de uma imagem que a API devolve como texto: o caminho de um avatar (`icones/avatares/casta-bago.svg`, relativo à
 * API) ou o `imagePath` de uma bebida, que pode ser o URL absoluto da imagem do retalhista. Se começa por `http(s)://`
 * usa-se tal como está; senão prefixa-se o endereço da API. `null` se não há caminho.
 */
fun resolveImageUrl(path: String?, baseUrl: String = BuildConfig.API_BASE_URL): String? {
    val caminho = path?.trim().orEmpty()
    return when {
        caminho.isEmpty() -> null
        caminho.startsWith("http://", ignoreCase = true) || caminho.startsWith("https://", ignoreCase = true) -> caminho
        else -> baseUrl.trimEnd('/') + "/" + caminho.trimStart('/')
    }
}
