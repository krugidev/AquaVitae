package pt.aquavitae.android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import pt.aquavitae.android.feature.compra.CompraPromptHost
import pt.aquavitae.android.navigation.AppNavHost
import pt.aquavitae.android.ui.theme.AquaVitaeTheme

/**
 * Única Activity da app (padrão single-activity + Navigation Compose).
 * Aloja o [AppNavHost], que gere todos os ecrãs do fluxo MVP.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Tem de ser antes do super.onCreate: troca o tema do ecrã de arranque pelo tema da app.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Edge-to-edge (obrigatório a partir do Android 15): o conteúdo desenha por baixo das barras do sistema, que ficam
        // transparentes com ícones ESCUROS (o fundo da app é claro). Sem isto os ícones da barra de estado ficavam
        // brancos sobre fundo branco, invisíveis. Cada ecrã aplica o espaço das barras com `systemBarsPadding()`.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        setContent {
            AquaVitaeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavHost()
                    // O "Compraste?" de depois de um clique em "Comprar": por cima de tudo, sempre que a app volta ao primeiro plano.
                    CompraPromptHost()
                }
            }
        }
    }
}
