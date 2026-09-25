package pt.aquavitae.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.ButtonShape
import pt.aquavitae.android.ui.theme.CardGray
import pt.aquavitae.android.ui.theme.CardShape
import pt.aquavitae.android.ui.theme.ChipGray

/**
 * O cartão dos ecrãs de recuperar password e de onboarding: cartão cinzento com um cabeçalho em pílula grená (título a
 * branco) sobreposto ao topo. Com `fillHeight` o corpo ocupa a altura que sobra (o cartão tem de ter altura limitada) e o
 * conteúdo pode usar `Modifier.weight` — é o caso dos ecrãs de preferências, com a navegação encostada ao fundo.
 */
@Composable
fun PillCard(
    title: String,
    modifier: Modifier = Modifier,
    fillHeight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().clip(CardShape).background(CardGray)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(58.dp).clip(ButtonShape).background(Burgundy),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = AquaText.Title,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
        Column(
            modifier = (if (fillHeight) Modifier.weight(1f) else Modifier)
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 16.dp, bottom = 18.dp),
            content = content,
        )
    }
}

/** O botão redondo de navegação: seta para trás (cinzento, seta preta) ou para a frente (grená, seta branca). */
@Composable
fun RoundNavButton(
    forward: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val container = if (forward) Burgundy else ChipGray
    val content = if (forward) Color.White else Color.Black
    Box(
        modifier = modifier
            .size(width = NavButtonWidth, height = 44.dp)
            .clip(RoundedCornerShape(50))
            .background(container)
            .clickable(enabled = !loading, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = content, strokeWidth = 2.5.dp)
        } else {
            Icon(
                imageVector = if (forward) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = if (forward) "Seguinte" else "Voltar",
                tint = content,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private val NavButtonWidth = 58.dp

/**
 * A linha de navegação do fundo do cartão: voltar à esquerda (sem `onBack` fica um espaço vazio), seguinte à direita e, no
 * meio, o que se quiser (ex.: "IGNORAR"). Com `forwardLoading` o botão de seguir mostra uma roda enquanto se guarda.
 */
@Composable
fun NavRow(
    onBack: (() -> Unit)?,
    onForward: () -> Unit,
    modifier: Modifier = Modifier,
    forwardLoading: Boolean = false,
    middle: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (onBack != null) RoundNavButton(forward = false, onClick = onBack) else Spacer(Modifier.width(NavButtonWidth))
        middle()
        RoundNavButton(forward = true, onClick = onForward, loading = forwardLoading)
    }
}

/** "PRÓXIMO →": o botão grená, mais estreito que o do login, ao centro do cartão (ecrãs 7 a 10 do onboarding). */
@Composable
fun NextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "PRÓXIMO →",
    loading: Boolean = false,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Button(
            onClick = onClick,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(0.78f).height(48.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Burgundy,
                contentColor = Color.White,
                disabledContainerColor = Burgundy,
                disabledContentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp, disabledElevation = 3.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
            } else {
                Text(text = text, style = AquaText.Next)
            }
        }
    }
}
