package pt.aquavitae.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.LocalePt
import pt.aquavitae.android.data.model.anoNoNome
import pt.aquavitae.android.data.model.formatarPrecoPt
import pt.aquavitae.android.data.model.linhaAtributos
import pt.aquavitae.android.data.model.textoReviews
import pt.aquavitae.android.data.network.resolveImageUrl
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.CardGray
import pt.aquavitae.android.ui.theme.MutedInk
import pt.aquavitae.android.ui.theme.Paper

/**
 * O cartão de bebida usado na homepage ("Escolhido para ti"), no catálogo e nas caves: imagem, nome + ano, produtor •
 * região, a linha de atributos ([pt.aquavitae.android.data.model.linhaAtributos]), rating, retalhista e preço mais
 * barato. Toque curto ou premido abrem o mesmo popup de detalhe (`BebidaDetalheSheet`) — o mockup só pede o premido
 * ("ao pressionar num item"), mas sem outro destino definido para o toque curto, fazem os dois o mesmo.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BebidaCard(bebida: BebidaSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val nome = bebida.nome.orEmpty()
    val ano = bebida.anoNoNome()
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(shape)
            .background(CardGray)
            .combinedClickable(onClick = onClick, onLongClick = onClick)
            .padding(10.dp),
    ) {
        Box(
            modifier = Modifier.width(76.dp).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(Paper),
            contentAlignment = Alignment.Center,
        ) {
            val url = resolveImageUrl(bebida.imagePath)
            if (url != null) {
                AsyncImage(model = url, contentDescription = nome, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Text(text = "Imagem\nda bebida", style = AquaText.Footer.copy(color = MutedInk), textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth()) {
                Text(text = nome, style = AquaText.BebidaNomeSerif, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (ano != null) Text(text = ano, style = AquaText.Footer.copy(color = MutedInk))
            }
            val produtorLinha = listOfNotNull(bebida.produtorNome, bebida.produtorRegiao).joinToString(" • ")
            if (produtorLinha.isNotEmpty()) Text(text = produtorLinha, style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp))
            Text(
                text = bebida.linhaAtributos(),
                style = AquaText.Footer.copy(color = MutedInk, fontSize = 10.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = String.format(LocalePt, "%.1f/5 • %s", bebida.ratingMedio, textoReviews(bebida.totalReviews)),
                style = AquaText.Label.copy(fontSize = 16.sp),
            )
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = bebida.retalhistaNome?.uppercase(LocalePt).orEmpty(),
                    style = AquaText.Footer.copy(color = MutedInk, fontSize = 9.sp),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                bebida.precoDesde?.let { Text(text = formatarPrecoPt(it), style = AquaText.Label) }
            }
        }
    }
}
