package pt.aquavitae.android.feature.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.ui.components.AquaVitaeLogo
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.LogoVariant
import pt.aquavitae.android.ui.components.PrimaryButton
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Paper

/** O ecrã de loading: o logótipo ao centro e os cinco pontos vermelhos em baixo, enquanto se verifica a sessão. */
@Composable
fun LoadingScreen(
    onLoggedIn: () -> Unit,
    onNeedsLogin: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            SessionState.LoggedIn -> onLoggedIn()
            SessionState.NeedsLogin -> onNeedsLogin()
            else -> Unit
        }
    }

    Box(Modifier.fillMaxSize().background(Paper).systemBarsPadding()) {
        AquaVitaeLogo(LogoVariant.Horizontal, Modifier.align(Alignment.Center))

        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 56.dp)) {
            val offline = state as? SessionState.Offline
            if (offline != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 36.dp),
                ) {
                    Text(text = offline.message, style = AquaText.Error, textAlign = TextAlign.Center)
                    PrimaryButton(text = "TENTAR DE NOVO", onClick = viewModel::check, modifier = Modifier.width(240.dp))
                }
            } else {
                LoadingDots()
            }
        }
    }
}
