package pt.aquavitae.android.ui.components

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import pt.aquavitae.android.ui.theme.ScrimBlack

/**
 * Põe o escurecimento atrás de um popup nos 43 % de preto medidos nos desenhos ([ScrimBlack]); o do Compose por omissão é
 * 60 %, mais pesado. Chamar dentro do conteúdo do `Dialog`.
 */
@Composable
fun DialogScrim(amount: Float = ScrimBlack.alpha) {
    val window = (LocalView.current.parent as? DialogWindowProvider)?.window
    SideEffect { window?.setDimAmount(amount) }
}

/**
 * Faz a janela de um `Dialog` ocupar o ecrã todo. Por omissão o Compose dimensiona a janela pelo conteúdo (`WRAP_CONTENT`
 * em altura, mesmo com `usePlatformDefaultWidth = false`, que só afeta a largura): um conteúdo com `Modifier.fillMaxHeight()`
 * mede-se então contra uma altura sem limite real, e o que passa da altura do ecrã fica fora da área visível sem aviso — foi
 * o que aconteceu ao botão "FECHAR" do [pt.aquavitae.android.feature.legal.TermsSheet]. Chamar dentro do conteúdo do `Dialog`,
 * antes de qualquer `fillMaxHeight()`.
 */
@Composable
fun DialogFillScreen() {
    val window = (LocalView.current.parent as? DialogWindowProvider)?.window
    SideEffect { window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT) }
}
