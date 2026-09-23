package pt.aquavitae.android

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe Application anotada com @HiltAndroidApp: ponto de entrada do grafo
 * de injeção de dependências (Hilt) para toda a app.
 *
 * É também a fábrica do carregador de imagens do Coil, que a app inteira usa: o [SvgDecoder] é o que deixa desenhar os
 * avatares (SVG servidos pela API); as imagens das bebidas (JPG/PNG dos retalhistas) não precisam de nada especial.
 */
@HiltAndroidApp
class AquaVitaeApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .components { add(SvgDecoder.Factory()) }
        .crossfade(true)
        .build()
}
