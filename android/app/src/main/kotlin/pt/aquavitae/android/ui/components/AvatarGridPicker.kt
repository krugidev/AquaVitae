package pt.aquavitae.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.data.network.resolveImageUrl
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.BurgundyTint

/**
 * Pílulas de categoria (Castas, Garrafas, Copos, ...) + grelha 3×3 de avatares dessa categoria — usado no onboarding
 * (`AvatarStep`). Tocar no já escolhido outra vez tira-o (`onSelect` chamado com o mesmo id de novo, quem trata o
 * toggle é o chamador). O popup "Escolher avatar" do ecrã de perfil (fatia 5) tem uma pílula extra ("Todos", sem
 * equivalente no onboarding) — usa a grelha (`AvatarTileGrid`, pública) sozinha, com as suas próprias pílulas.
 */
@Composable
fun AvatarGridPicker(
    categorias: List<LookupItem>,
    avatares: List<Avatar>,
    categoriaId: Long?,
    avatarId: Long?,
    onCategoria: (Long) -> Unit,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            categorias.forEach { categoria ->
                PillChip(text = categoria.nome.orEmpty(), selected = categoria.id == categoriaId, onClick = { onCategoria(categoria.id) })
            }
        }
        Spacer(Modifier.height(14.dp))
        AvatarTileGrid(avatares = avatares.filter { it.categoriaId == categoriaId }, selectedId = avatarId, onSelect = onSelect)
    }
}

private const val AVATARES_POR_LINHA = 3

/** Só a grelha 3×3 (sem as pílulas de categoria) — usada sozinha quando o chamador tem pílulas próprias. */
@Composable
fun AvatarTileGrid(avatares: List<Avatar>, selectedId: Long?, onSelect: (Long) -> Unit) {
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
