package pt.aquavitae.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.aquavitae.android.R
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.Paper

/** Os 5 destinos da barra: os 4 laterais têm rótulo; o do meio (Home) é o botão redondo elevado, sem rótulo. */
enum class BottomNavItem(val icon: Int, val label: String) {
    Catalogo(R.drawable.ic_nav_catalogo, "CATÁLOGO"),
    Cave(R.drawable.ic_nav_cave, "CAVE"),
    Home(R.drawable.ic_nav_home, ""),
    Wishlist(R.drawable.ic_nav_wishlist, "WISHLIST"),
    Favoritos(R.drawable.ic_nav_favoritos, "FAVORITOS"),
}

private val ladoEsquerdo = listOf(BottomNavItem.Catalogo, BottomNavItem.Cave)
private val ladoDireito = listOf(BottomNavItem.Wishlist, BottomNavItem.Favoritos)
private val BarShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
private val CentroDiametro = 62.dp

/**
 * A barra de navegação principal (grená, com o botão "Home" elevado ao centro). Os ícones são os PNG entregues pelo
 * utilizador (`res/drawable/ic_nav_*`), transparentes: tingem-se com [ColorFilter.tint] conforme o estado — sem precisar
 * de os reexportar. Usa-se envolvendo o conteúdo de cada ecrã de topo (ver `AppNavHost`, "Ecrãs com barra de navegação").
 */
@Composable
fun BottomNavBar(selected: BottomNavItem, onSelect: (BottomNavItem) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BarShape)
                .background(Burgundy)
                .navigationBarsPadding()
                .padding(top = 14.dp, bottom = 10.dp, start = 12.dp, end = 12.dp)
                .height(50.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ladoEsquerdo.forEach { NavItem(it, it == selected, onSelect, Modifier.weight(1f)) }
            // Espaço para o botão central, que se sobrepõe por cima (ver Box abaixo).
            Box(Modifier.weight(1f))
            ladoDireito.forEach { NavItem(it, it == selected, onSelect, Modifier.weight(1f)) }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-CentroDiametro / 2) + 4.dp)
                .size(CentroDiametro)
                .clip(CircleShape)
                .background(Paper)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Burgundy)
                .clickable(role = Role.Button, onClick = { onSelect(BottomNavItem.Home) }),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(BottomNavItem.Home.icon),
                contentDescription = "Início",
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun RowScope.NavItem(item: BottomNavItem, selected: Boolean, onSelect: (BottomNavItem) -> Unit, modifier: Modifier) {
    val tint = if (selected) Color.White else Color.White.copy(alpha = 0.62f)
    Column(
        modifier = modifier
            .clickable(role = Role.Tab, onClick = { onSelect(item) }),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(painter = painterResource(item.icon), contentDescription = item.label, tint = tint, modifier = Modifier.size(22.dp))
        Text(text = item.label, style = AquaText.Footer.copy(color = tint, fontSize = 9.sp), maxLines = 1)
    }
}

/** Padding para o conteúdo de um ecrã com [BottomNavBar]: a altura da barra + a folga do botão central elevado. */
val BottomNavContentPadding = PaddingValues(bottom = 88.dp)
