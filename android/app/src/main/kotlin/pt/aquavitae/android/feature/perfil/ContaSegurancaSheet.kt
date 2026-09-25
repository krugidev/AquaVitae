package pt.aquavitae.android.feature.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.ui.components.CabecalhoFolha
import pt.aquavitae.android.ui.components.FolhaInferior
import pt.aquavitae.android.ui.components.tituloComDestaque
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint
import pt.aquavitae.android.ui.theme.ButtonShape
import pt.aquavitae.android.ui.theme.ErrorRed
import pt.aquavitae.android.ui.theme.Ink
import pt.aquavitae.android.ui.theme.MutedInk

private enum class PassoConta { Nenhum, AlterarPassword, ApagarConta }

/**
 * "Conta e segurança" (`android/design/perfil/05-reviews-e-conta-seguranca.png`, imagem 9b): o email da conta (só leitura), "Alterar
 * password" e "Apagar conta". Abre-se do perfil e da linha "Email e password" do "Editar perfil". **Diferença combinada com o
 * utilizador:** o "Alterar password" não pede a password atual — usa o código enviado por email, o mesmo sistema da recuperação de
 * password (com o temporizador de 15 minutos). `onSessaoTerminada`: a conta foi apagada (ou a sessão não se manteve) — o ecrã que
 * abriu a folha leva ao login.
 */
@Composable
fun ContaSegurancaSheet(
    onDismiss: () -> Unit,
    onSessaoTerminada: () -> Unit,
    viewModel: ContaSegurancaViewModel = hiltViewModel(key = "conta-seguranca"),
) {
    LaunchedEffect(Unit) { viewModel.carregar() }
    val utilizador by viewModel.utilizador.collectAsState()
    var passo by remember { mutableStateOf(PassoConta.Nenhum) }

    FolhaInferior(onDismiss = onDismiss) {
        CabecalhoFolha(titulo = tituloComDestaque("Conta e ", "segurança"), onFechar = onDismiss)
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(BurgundyTint.copy(alpha = 0.25f)).padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(text = "EMAIL DA CONTA", style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
                Text(text = utilizador?.email ?: "…", style = AquaText.Field.copy(fontSize = 15.sp))
            }
            Spacer(Modifier.height(6.dp))
            LinhaConta(
                icone = Icons.Filled.Lock,
                titulo = "Alterar password",
                subtitulo = "Enviamos um código para o teu email para confirmares que és tu.",
                destrutiva = false,
                ativa = utilizador?.email != null,
                onClick = { passo = PassoConta.AlterarPassword },
            )
            HorizontalDivider(thickness = 1.dp, color = BurgundyTint)
            LinhaConta(
                icone = Icons.Filled.Delete,
                titulo = "Apagar conta",
                subtitulo = "Remove caves, wishlist, favoritos e reviews. Não pode ser desfeito.",
                destrutiva = true,
                ativa = utilizador != null,
                onClick = { passo = PassoConta.ApagarConta },
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = ButtonShape,
                border = BorderStroke(1.dp, Ink),
            ) { Text(text = "Fechar", style = AquaText.Label.copy(color = Ink, fontSize = 15.sp)) }
            Spacer(Modifier.height(20.dp))
        }
    }

    when (passo) {
        PassoConta.Nenhum -> Unit
        PassoConta.AlterarPassword -> AlterarPasswordSheet(
            email = utilizador?.email.orEmpty(),
            onDismiss = { passo = PassoConta.Nenhum },
            onSessaoTerminada = onSessaoTerminada,
        )
        PassoConta.ApagarConta -> ApagarContaDialog(
            utilizador = utilizador,
            onDismiss = { passo = PassoConta.Nenhum },
            onSessaoTerminada = onSessaoTerminada,
        )
    }
}

@Composable
private fun LinhaConta(icone: ImageVector, titulo: String, subtitulo: String, destrutiva: Boolean, ativa: Boolean, onClick: () -> Unit) {
    val cor = if (destrutiva) ErrorRed else Ink
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = ativa, onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(if (destrutiva) ErrorRed.copy(alpha = 0.12f) else BurgundyTint.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = if (destrutiva) ErrorRed else Ink, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(text = titulo, style = AquaText.Label.copy(fontSize = 15.sp, color = cor))
            Text(text = subtitulo, style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = if (destrutiva) ErrorRed else Burgundy, modifier = Modifier.size(20.dp))
    }
}
