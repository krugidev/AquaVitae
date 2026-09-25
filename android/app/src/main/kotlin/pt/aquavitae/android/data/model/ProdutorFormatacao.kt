package pt.aquavitae.android.data.model

import java.net.URLEncoder
import java.util.Locale

/** O site do produtor como aparece na pílula da página ("https://www.ferreirinha.pt/vinhos" → "ferreirinha.pt"); `null` se não há site. */
fun ProdutorDetail.websiteParaMostrar(): String? =
    website?.trim()?.takeIf { it.isNotEmpty() }?.let { url ->
        url.removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore('/').trimEnd('.')
    }?.takeIf { it.isNotEmpty() }

/** O URL do site pronto a abrir num browser: acrescenta `https://` quando a BD só tem o domínio. */
fun ProdutorDetail.websiteUrl(): String? =
    website?.trim()?.takeIf { it.isNotEmpty() }?.let { if (it.startsWith("http://") || it.startsWith("https://")) it else "https://$it" }

/** A morada em texto, sem espaços a mais; `null` se não há (ou está em branco). */
val ProdutorDetail.moradaParaMostrar: String? get() = morada?.trim()?.takeIf { it.isNotEmpty() }

/** Só há ponto exato se o produtor tem as duas coordenadas. */
val ProdutorDetail.temCoordenadas: Boolean get() = latitude != null && longitude != null

/** O cartão "Localização" aparece se há morada **ou** coordenadas (uma delas chega para "Abrir no mapa"). */
val ProdutorDetail.temLocalizacao: Boolean get() = moradaParaMostrar != null || temCoordenadas

/** "41.1621, -7.7891": com ponto decimal e 4 casas (~10 m) — são coordenadas, não um valor em PT-PT. */
fun ProdutorDetail.coordenadasTexto(): String? {
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    return String.format(Locale.US, "%.4f, %.4f", lat, lon)
}

private fun escapar(texto: String): String = URLEncoder.encode(texto, "UTF-8").replace("+", "%20")

/**
 * O URI `geo:` que abre a app de mapas do telemóvel. **Com coordenadas** usa o ponto exato, com o nome como etiqueta do
 * marcador (`geo:lat,lon?q=lat,lon(Nome)`) — a morada postal de uma quinta pode ser a da sede, não a das vinhas.
 * **Sem coordenadas** pesquisa pela morada (`geo:0,0?q=morada`). `null` se não há nenhuma das duas.
 */
fun ProdutorDetail.geoUri(): String? {
    val lat = latitude
    val lon = longitude
    if (lat != null && lon != null) {
        val ponto = String.format(Locale.US, "%.6f,%.6f", lat, lon)
        val etiqueta = nome?.takeIf { it.isNotBlank() }?.let { "(${escapar(it)})" }.orEmpty()
        return "geo:$ponto?q=$ponto$etiqueta"
    }
    return moradaParaMostrar?.let { "geo:0,0?q=${escapar(it)}" }
}

/** O plano B se não houver app de mapas: o mesmo sítio no Google Maps pelo browser. */
fun ProdutorDetail.mapaWebUrl(): String? {
    val lat = latitude
    val lon = longitude
    if (lat != null && lon != null) return String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f", lat, lon)
    return moradaParaMostrar?.let { "https://www.google.com/maps/search/?api=1&query=${escapar(it)}" }
}

/** "1 garrafa" / "3 garrafas" (a pílula da página mostra em maiúsculas). */
fun garrafasTexto(total: Int): String = if (total == 1) "1 garrafa" else "$total garrafas"
