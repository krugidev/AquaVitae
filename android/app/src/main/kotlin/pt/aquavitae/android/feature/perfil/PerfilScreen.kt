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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.aquavitae.android.data.model.LocalePt
import pt.aquavitae.android.ui.components.AvatarBadge
import pt.aquavitae.android.ui.components.LinkText
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.components.PillChip
import pt.aquavitae.android.ui.components.iniciaisDe
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper
import pt.aquavitae.android.ui.theme.RoseBorder
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.TextStyle as JavaTextStyle

/**
 * "Detalhes do perfil" (`android/design/perfil/01-perfil.png`): dados + estatísticas + resumo das preferências
 * (editável num popup à parte). "As minhas reviews" e "Conta e segurança" ficam sem destino por agora — os mockups
 * desses dois ainda não chegaram (o utilizador disse que os envia a seguir); "Histórico de provadas" e "As minhas
 * caves" já reaproveitam os ecrãs feitos nas fatias 3b/3c.
 */
@Composable
fun PerfilScreen(
    onVoltar: () -> Unit,
    onEditarPerfil: () -> Unit,
    onVerProvadas: () -> Unit,
    onVerCaves: () -> Unit,
    onSessaoTerminada: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    // "Editar perfil"/"Editar preferências" mudam dados que este ecrã mostra; voltar a ele (de dentro do próprio
    // NavHost) desmonta e volta a montar este Composable, por isso isto recarrega sempre que reaparece — o mesmo
    // padrão do FavoritosScreen/WishlistScreen (ver CLAUDE.md), aqui numa pilha normal de navegação, não numa aba.
    LaunchedEffect(Unit) { viewModel.carregar() }

    Box(Modifier.fillMaxSize().background(Paper).statusBarsPadding()) {
        when (val estado = state) {
            PerfilUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }

            is PerfilUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = estado.message, style = AquaText.Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                LinkText(text = "TENTAR DE NOVO", onClick = viewModel::carregar)
            }

            is PerfilUiState.Ready -> {
                PerfilContent(
                    state = estado,
                    viewModel = viewModel,
                    onVoltar = onVoltar,
                    onEditarPerfil = onEditarPerfil,
                    onVerProvadas = onVerProvadas,
                    onVerCaves = onVerCaves,
                    onSessaoTerminada = onSessaoTerminada,
                )
                if (estado.mostrarEditarPreferencias) {
                    EditarPreferenciasSheet(
                        preferenciasAtuais = estado.preferencias,
                        onDismiss = viewModel::fecharEditarPreferencias,
                        onGuardado = viewModel::preferenciasAtualizadas,
                    )
                }
            }
        }
    }
}

@Composable
private fun PerfilContent(
    state: PerfilUiState.Ready,
    viewModel: PerfilViewModel,
    onVoltar: () -> Unit,
    onEditarPerfil: () -> Unit,
    onVerProvadas: () -> Unit,
    onVerCaves: () -> Unit,
    onSessaoTerminada: () -> Unit,
) {
    val utilizador = state.utilizador
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Burgundy) }
                Spacer(Modifier.weight(1f))
                LinkText(text = "EDITAR PERFIL", onClick = onEditarPerfil, style = AquaText.Footer.copy(fontSize = 11.sp))
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarBadge(
                    avatar = utilizador.avatar,
                    iniciais = iniciaisDe(utilizador.firstName, utilizador.lastName, utilizador.username),
                    size = 64.dp,
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(text = utilizador.nomeParaMostrar.orEmpty(), style = AquaText.BebidaNomeSerif.copy(fontSize = 19.sp))
                    Text(text = "@${utilizador.username.orEmpty()}", style = AquaText.Footer.copy(color = MutedInk, fontSize = 12.sp))
                    val membroDesde = formatarMembroDesde(utilizador.accountCreatedAt)
                    val linha = listOfNotNull(utilizador.nationality, membroDesde).joinToString(" · ")
                    if (linha.isNotBlank()) Text(text = linha, style = AquaText.Footer.copy(color = MutedInk, fontSize = 12.sp))
                }
            }
        }
        if (!utilizador.bioDesc.isNullOrBlank()) {
            item { Text(text = utilizador.bioDesc, style = AquaText.Field.copy(fontSize = 14.sp)) }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PerfilStatBox("PROVADAS", utilizador.totalProvadas, Modifier.weight(1f))
                    PerfilStatBox("REVIEWS", utilizador.totalReviews, Modifier.weight(1f))
                    PerfilStatBox("FAVORITOS", utilizador.totalFavoritos, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PerfilStatBox("WISHLIST", utilizador.totalWishlist, Modifier.weight(1f))
                    PerfilStatBox("CAVES", utilizador.totalCaves, Modifier.weight(1f))
                    PerfilStatBox("GARRAFAS", utilizador.totalGarrafas, Modifier.weight(1f))
                }
            }
        }
        item { PreferenciasCard(state, onEditar = viewModel::abrirEditarPreferencias) }
        item {
            PerfilNavRow(
                titulo = "Histórico de provadas",
                subtitulo = "${utilizador.totalProvadas} bebidas, da mais recente",
                onClick = onVerProvadas,
            )
        }
        item { PerfilNavRow(titulo = "As minhas reviews", subtitulo = "${utilizador.totalReviews} publicadas", onClick = null) }
        item { PerfilNavRow(titulo = "As minhas caves", subtitulo = "${utilizador.totalCaves} caves", onClick = onVerCaves) }
        item { PerfilNavRow(titulo = "Conta e segurança", subtitulo = "email, password, nacionalidade", onClick = null) }
        item {
            LinkText(
                text = "Terminar sessão",
                onClick = { viewModel.terminarSessao(onSessaoTerminada) },
                style = AquaText.Link,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun PerfilStatBox(label: String, valor: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(modifier = modifier.clip(shape).border(1.dp, RoseBorder, shape).padding(horizontal = 10.dp, vertical = 12.dp)) {
        Text(text = valor.toString(), style = AquaText.BebidaNomeSerif.copy(fontSize = 22.sp, color = Burgundy))
        Text(text = label, style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp))
    }
}

@Composable
private fun PreferenciasCard(state: PerfilUiState.Ready, onEditar: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, RoseBorder, shape).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "AS MINHAS PREFERÊNCIAS", style = AquaText.Footer.copy(fontSize = 11.sp, color = MutedInk), modifier = Modifier.weight(1f))
            LinkText(text = "EDITAR", onClick = onEditar, style = AquaText.Footer.copy(fontSize = 11.sp))
        }
        Spacer(Modifier.height(10.dp))
        if (state.categoriaNomes.isEmpty()) {
            Text(text = "Ainda não escolheste nenhuma.", style = AquaText.SmallLink.copy(color = MutedInk))
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.categoriaNomes) { nome -> PillChip(text = nome, selected = true, onClick = {}) }
            }
        }
        Spacer(Modifier.height(12.dp))
        PreferenciaLinha("Acidez", intervaloTexto(state.preferencias.acidezMin, state.preferencias.acidezMax))
        PreferenciaLinha("Doçura", intervaloTexto(state.preferencias.docuraMin, state.preferencias.docuraMax))
        PreferenciaLinha("Castas", state.castaNomes.joinToString(", ").ifBlank { "—" })
    }
}

@Composable
private fun PreferenciaLinha(rotulo: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Text(text = rotulo, style = AquaText.Footer.copy(color = MutedInk, fontSize = 12.sp), modifier = Modifier.width(70.dp))
        Text(text = valor, style = AquaText.Footer.copy(fontSize = 12.sp), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PerfilNavRow(titulo: String, subtitulo: String, onClick: (() -> Unit)?) {
    Column(
        Modifier.fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(text = titulo, style = AquaText.Label.copy(fontSize = 14.sp))
                Text(text = subtitulo, style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
            }
            if (onClick != null) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MutedInk)
            }
        }
    }
}

private fun intervaloTexto(min: Int?, max: Int?): String = if (min != null && max != null) "$min a $max" else "—"

/** "membro desde março de 2026" a partir do `accountCreatedAt` ISO. */
private fun formatarMembroDesde(dataIso: String?): String? {
    if (dataIso == null) return null
    return runCatching {
        val data = Instant.parse(dataIso).atZone(ZoneOffset.UTC)
        val mes = data.month.getDisplayName(JavaTextStyle.FULL, LocalePt)
        "membro desde $mes de ${data.year}"
    }.getOrNull()
}
