package pt.aquavitae.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.CardGray
import pt.aquavitae.android.ui.theme.CardShape
import pt.aquavitae.android.ui.theme.Paper

/** O cartão cinzento arredondado onde ficam os campos. */
@Composable
fun AuthCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(CardGray)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        content = content,
    )
}

/**
 * Faz o elemento sobrepor-se `overlap` ao que vem antes (o botão "LOGIN" fica a meio da margem inferior do cartão).
 * Ao contrário de `offset`, também encolhe o espaço que ocupa, por isso o que vem a seguir sobe com ele.
 */
fun Modifier.overlapTop(overlap: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val px = overlap.roundToPx()
    layout(placeable.width, placeable.height - px) { placeable.place(0, -px) }
}

/**
 * Esqueleto dos ecrãs de autenticação: fundo branco, conteúdo centrado e com scroll (o teclado nunca tapa os campos)
 * e "TERMOS E CONDIÇÕES" fixo no fundo. Quem precisar de mexer no scroll (ex.: rolar até ao fim quando o teclado abre)
 * passa o seu `scrollState`.
 */
@Composable
fun AuthScaffold(
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize().background(Paper).systemBarsPadding().imePadding()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 36.dp)
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
        Text(
            text = "TERMOS E CONDIÇÕES",
            style = AquaText.Footer,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onOpenTerms)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}
