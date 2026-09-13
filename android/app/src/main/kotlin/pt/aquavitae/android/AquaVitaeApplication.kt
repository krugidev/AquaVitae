package pt.aquavitae.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe Application anotada com @HiltAndroidApp: ponto de entrada do grafo
 * de injeção de dependências (Hilt) para toda a app.
 */
@HiltAndroidApp
class AquaVitaeApplication : Application()
