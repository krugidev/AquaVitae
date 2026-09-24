package pt.aquavitae.android.feature.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.Nacionalidade
import pt.aquavitae.android.ui.components.AvatarBadge
import pt.aquavitae.android.ui.components.FlagChip
import pt.aquavitae.android.ui.components.FloatingLabel
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.UnderlineField
import pt.aquavitae.android.ui.components.flagEmoji
import pt.aquavitae.android.ui.components.iniciaisDe
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper

/**
 * "Editar perfil" (`android/design/perfil/02-editar-perfil.png`): nome, apelido, username, nacionalidade,
 * descrição/bio (com contador e emojis — o teclado do sistema já os suporta, sem código extra: a fonte Inter não
 * tem os glifos, mas o Android troca sozinho para a fonte de emoji do sistema nesse caso) e avatar (popup à parte).
 * **Diferença combinada com o utilizador:** por agora mostra-se o email como texto simples por baixo da bio, em vez
 * da linha "Email e password" do mockup (essa linha pede o ecrã "Conta e segurança", ainda sem mockup).
 */
@Composable
fun EditarPerfilScreen(
    onCancelar: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: EditarPerfilViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(Modifier.fillMaxSize().background(Paper).statusBarsPadding()) {
        when (val estado = state) {
            EditarPerfilUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }

            is EditarPerfilUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                LinkText(text = "TENTAR DE NOVO", onClick = viewModel::carregar)
            }

            is EditarPerfilUiState.Ready -> {
                EditarPerfilContent(estado, viewModel, onCancelar, onGuardado)
                if (estado.mostrarEscolherAvatar) {
                    EscolherAvatarSheet(
                        avatarIdAtual = estado.avatar?.id,
                        onDismiss = viewModel::fecharEscolherAvatar,
                        onConfirmar = viewModel::onAvatarEscolhido,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditarPerfilContent(
    state: EditarPerfilUiState.Ready,
    viewModel: EditarPerfilViewModel,
    onCancelar: () -> Unit,
    onGuardado: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                LinkText(text = "Cancelar", onClick = onCancelar, style = AquaText.Footer.copy(fontSize = 12.sp), textAlign = TextAlign.Start)
                Spacer(Modifier.weight(1f))
                Text(text = "EDITAR PERFIL", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
                Spacer(Modifier.weight(1f))
                if (state.aGuardar) {
                    Text(text = "A guardar…", style = AquaText.Footer.copy(color = MutedInk, fontSize = 12.sp))
                } else {
                    LinkText(text = "Guardar", onClick = { viewModel.guardar(onGuardado) }, style = AquaText.Footer.copy(fontSize = 12.sp))
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    AvatarBadge(
                        avatar = state.avatar,
                        iniciais = state.avatarIniciais,
                        size = 84.dp,
                        onClick = viewModel::abrirEscolherAvatar,
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinkText(text = "Mudar avatar", onClick = viewModel::abrirEscolherAvatar, style = AquaText.SmallLink)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                UnderlineField(
                    value = state.firstName,
                    onValueChange = viewModel::onFirstName,
                    label = "Nome próprio",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
                UnderlineField(
                    value = state.lastName,
                    onValueChange = viewModel::onLastName,
                    label = "Apelido",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
            }
        }
        item { UsernameField(state.username, viewModel::onUsername) }
        item {
            NationalityField(state.nacionalidades, selectedId = state.nationalityId, onSelect = viewModel::onNationality)
        }
        item { DescriptionBox(value = state.bioDesc, onValueChange = viewModel::onBio) }
        if (state.email != null) {
            item {
                Column {
                    Text(text = "Email", style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
                    Text(text = state.email, style = AquaText.Field.copy(fontSize = 14.sp))
                }
            }
        }
        state.erro?.let { erro ->
            item { Text(text = erro, style = AquaText.Error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
        }
    }
}

/** "@username", sem espaços (filtrados ao escrever), com o contador 3–30 por baixo. */
@Composable
private fun UsernameField(value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(text = "USERNAME", style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp))
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "@", style = AquaText.Field.copy(color = Burgundy, fontWeight = FontWeight.Bold), modifier = Modifier.padding(end = 2.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = AquaText.Field,
                cursorBrush = SolidColor(Burgundy),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(thickness = 1.5.dp, color = Burgundy, modifier = Modifier.padding(top = 4.dp))
        Text(
            text = "${value.length} / 30 · único na app (a gravar avisa se já estiver em uso)",
            style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** O campo "Nacionalidade": mostra o que está escolhido e a sua bandeira, e abre uma lista ao tocar (igual ao do onboarding). */
@Composable
private fun NationalityField(nacionalidades: List<Nacionalidade>, selectedId: Long?, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selecionada = nacionalidades.firstOrNull { it.id == selectedId }

    Box {
        Column(Modifier.fillMaxWidth().clickable(role = Role.DropdownList) { expanded = true }) {
            Box(Modifier.fillMaxWidth().height(52.dp)) {
                FloatingLabel(label = "Nacionalidade", floated = selecionada != null, color = Burgundy, modifier = Modifier.align(Alignment.CenterStart))
                Row(
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = selecionada?.nome.orEmpty(), style = AquaText.Field, modifier = Modifier.weight(1f))
                    FlagChip(flag = flagEmoji(selecionada?.codigoPais))
                }
            }
            HorizontalDivider(thickness = 1.5.dp, color = Burgundy)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            nacionalidades.forEach { nacionalidade ->
                DropdownMenuItem(
                    text = { Text(text = listOfNotNull(flagEmoji(nacionalidade.codigoPais), nacionalidade.nome).joinToString("  ")) },
                    onClick = { onSelect(nacionalidade.id); expanded = false },
                )
            }
        }
    }
}

/** A caixa da descrição do perfil, com contador — igual à do onboarding, com o contador acrescentado aqui. */
@Composable
private fun DescriptionBox(value: String, onValueChange: (String) -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Column(Modifier.fillMaxWidth().clip(shape).border(1.5.dp, Burgundy, shape).padding(horizontal = 14.dp, vertical = 12.dp)) {
        Text(text = "Descrição do perfil", style = AquaText.Label)
        HorizontalDivider(thickness = 1.5.dp, color = Burgundy, modifier = Modifier.padding(top = 6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = AquaText.Field,
            cursorBrush = SolidColor(Burgundy),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp).padding(top = 10.dp),
        )
        Text(
            text = "${value.length} / 1000",
            style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp),
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
