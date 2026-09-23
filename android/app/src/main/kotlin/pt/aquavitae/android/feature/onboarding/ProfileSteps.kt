package pt.aquavitae.android.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.model.Nacionalidade
import pt.aquavitae.android.data.network.resolveImageUrl
import pt.aquavitae.android.ui.components.FlagChip
import pt.aquavitae.android.ui.components.FloatingLabel
import pt.aquavitae.android.ui.components.LookupContent
import pt.aquavitae.android.ui.components.NextButton
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.components.PillCard
import pt.aquavitae.android.ui.components.UnderlineField
import pt.aquavitae.android.ui.components.flagEmoji
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint

/** Ecrã 7 — "QUAL É O TEU NOME ?": nome próprio e apelido (ambos opcionais). */
@Composable
fun NameStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    val focusManager = LocalFocusManager.current
    val next = {
        focusManager.clearFocus()
        viewModel.next()
    }

    PillCard(title = "QUAL É O TEU NOME ?") {
        UnderlineField(
            value = state.firstName,
            onValueChange = viewModel::onFirstName,
            label = "Nome próprio",
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(4.dp))
        UnderlineField(
            value = state.lastName,
            onValueChange = viewModel::onLastName,
            label = "Apelido",
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { next() }),
        )
        Spacer(Modifier.height(24.dp))
        NextButton(onClick = next)
    }
}

/** Ecrã 8 — "ESTAMOS QUASE LÁ": nacionalidade (com bandeira) e descrição do perfil (ambas opcionais). */
@Composable
fun NationalityStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    val focusManager = LocalFocusManager.current

    PillCard(title = "ESTAMOS QUASE LÁ") {
        LookupContent(state.nacionalidades, onRetry = viewModel::carregarLookups) { nacionalidades ->
            NationalityField(nacionalidades, selectedId = state.nationalityId, onSelect = viewModel::onNationality)
        }
        Spacer(Modifier.height(16.dp))
        DescriptionBox(value = state.bioDesc, onValueChange = viewModel::onBio)
        Spacer(Modifier.height(22.dp))
        NextButton(onClick = {
            focusManager.clearFocus()
            viewModel.next()
        })
    }
}

/** O campo "Nacionalidade": mostra o que está escolhido e a sua bandeira, e abre uma lista ao tocar. */
@Composable
private fun NationalityField(
    nacionalidades: List<Nacionalidade>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selecionada = nacionalidades.firstOrNull { it.id == selectedId }

    Box {
        Column(Modifier.fillMaxWidth().clickable(role = Role.DropdownList) { expanded = true }) {
            Box(Modifier.fillMaxWidth().height(52.dp)) {
                FloatingLabel(
                    label = "Nacionalidade",
                    floated = selecionada != null,
                    color = Burgundy,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
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
                    onClick = {
                        onSelect(nacionalidade.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** A caixa da descrição do perfil: rótulo com o lápis no topo e o texto por baixo (até 1000 caracteres). */
@Composable
private fun DescriptionBox(value: String, onValueChange: (String) -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.5.dp, Burgundy, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Descrição do perfil", style = AquaText.Label, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.Edit, contentDescription = null, tint = Burgundy, modifier = Modifier.size(20.dp))
        }
        HorizontalDivider(thickness = 1.5.dp, color = Burgundy, modifier = Modifier.padding(top = 6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = AquaText.Field,
            cursorBrush = SolidColor(Burgundy),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .padding(top = 10.dp)
                .semantics { contentDescription = "Descrição do perfil" },
        )
    }
}

/**
 * Ecrã 9 — "ESCOLHE UM AVATAR": pílulas de categoria (Castas, Garrafas, Copos) e a grelha de 3×3 avatares dessa categoria.
 * Os avatares são SVG servidos pela API. Tocar no escolhido outra vez tira-o.
 */
@Composable
fun AvatarStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    PillCard(title = "ESCOLHE UM AVATAR") {
        LookupContent(state.avatarCategorias, onRetry = viewModel::carregarLookups) { categorias ->
            val categoriaId = state.avatarCategoriaId ?: categorias.firstOrNull()?.id
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                categorias.forEach { categoria ->
                    PillChip(
                        text = categoria.nome.orEmpty(),
                        selected = categoria.id == categoriaId,
                        onClick = { viewModel.onAvatarCategoria(categoria.id) },
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            LookupContent(state.avatares, onRetry = viewModel::carregarLookups) { avatares ->
                AvatarGrid(
                    avatares = avatares.filter { it.categoriaId == categoriaId },
                    selectedId = state.avatarId,
                    onSelect = viewModel::onAvatar,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        NextButton(onClick = viewModel::next)
    }
}

@Composable
private fun AvatarGrid(avatares: List<Avatar>, selectedId: Long?, onSelect: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        avatares.chunked(AVATARES_POR_LINHA).forEach { linha ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                linha.forEach { avatar ->
                    AvatarTile(
                        avatar = avatar,
                        selected = avatar.id == selectedId,
                        onClick = { onSelect(avatar.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // A última linha, se incompleta, mantém as casas do tamanho das outras.
                repeat(AVATARES_POR_LINHA - linha.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private const val AVATARES_POR_LINHA = 3

@Composable
private fun AvatarTile(avatar: Avatar, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(if (selected) BurgundyTint else Color.Transparent)
            .border(if (selected) 3.dp else 1.5.dp, Burgundy, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = resolveImageUrl(avatar.path),
            contentDescription = avatar.nome,
            modifier = Modifier.fillMaxSize(0.66f),
        )
    }
}
