package pt.aquavitae.android.ui.components

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pt.aquavitae.android.data.model.CodigoInfo
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.MutedInk

/**
 * Os dois contadores do ecrã do código (recuperar password e "Alterar password"): quanto falta para o código **expirar** e
 * quanto falta para se poder **pedir outro**. Arrancam quando a app recebe a resposta do pedido do código (`agoraMs` de um
 * relógio monotónico, `SystemClock.elapsedRealtime()`: mudar a hora do telemóvel não os mexe) com os valores que o servidor
 * manda — os mesmos para qualquer conta, para não revelar se ela existe.
 *
 * **Aproximação assumida:** se o pedido caiu dentro do intervalo mínimo de um anterior, o servidor não gera código novo e o em vigor
 * pode expirar até esse intervalo (60 s) mais cedo do que a contagem diz; nesse caso o servidor recusa com "Código expirado".
 */
data class CodigoContagem(val expiraEmMs: Long, val novoPedidoEmMs: Long) {

    fun segundosParaExpirar(agoraMs: Long): Long = segundosAte(expiraEmMs, agoraMs)

    fun expirou(agoraMs: Long): Boolean = agoraMs >= expiraEmMs

    fun segundosParaNovoPedido(agoraMs: Long): Long = segundosAte(novoPedidoEmMs, agoraMs)

    fun podePedirNovo(agoraMs: Long): Boolean = agoraMs >= novoPedidoEmMs

    companion object {
        fun aPartirDe(info: CodigoInfo, agoraMs: Long) = CodigoContagem(
            expiraEmMs = agoraMs + info.validadeSegundos * 1000,
            novoPedidoEmMs = agoraMs + info.novoPedidoEmSegundos * 1000,
        )
    }
}

// Arredonda para cima: só mostra 0 quando o tempo acabou mesmo (00:00 com meio segundo por passar seria enganador).
private fun segundosAte(fimMs: Long, agoraMs: Long): Long = if (fimMs <= agoraMs) 0 else (fimMs - agoraMs + 999) / 1000

/** "14:32" — minutos e segundos com dois dígitos. */
fun formatarContagem(segundos: Long): String {
    val total = segundos.coerceAtLeast(0)
    return "%02d:%02d".format(total / 60, total % 60)
}

/** O instante de agora para as contagens: monotónico, para bater com o de [CodigoContagem.aPartirDe]. */
fun agoraParaContagem(): Long = SystemClock.elapsedRealtime()

/**
 * Por baixo do campo do código: "O código expira em 14:32" (a contar), e "PEDIR NOVO CÓDIGO" quando já se pode (ou "Podes pedir outro
 * dentro de 45 s" até lá). Quando o tempo acaba, "O código expirou." a vermelho. Não faz nada sem contagem (o pedido ainda não voltou).
 */
@Composable
fun CodigoValidade(
    contagem: CodigoContagem?,
    onPedirNovo: () -> Unit,
    aPedir: Boolean,
    modifier: Modifier = Modifier,
) {
    if (contagem == null) return
    // Volta a arrancar (e a ler o relógio) sempre que chega uma contagem nova (um código novo).
    val agora by produceState(initialValue = agoraParaContagem(), contagem) {
        while (true) {
            value = agoraParaContagem()
            delay(500)
        }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (contagem.expirou(agora)) {
            Text(text = "O código expirou.", style = AquaText.Error, textAlign = TextAlign.Center)
        } else {
            Text(
                text = "O código expira em ${formatarContagem(contagem.segundosParaExpirar(agora))}",
                style = AquaText.Footer.copy(color = MutedInk, fontSize = 12.sp),
                textAlign = TextAlign.Center,
            )
        }
        if (contagem.podePedirNovo(agora)) {
            LinkText(
                text = if (aPedir) "A ENVIAR…" else "PEDIR NOVO CÓDIGO",
                onClick = { if (!aPedir) onPedirNovo() },
                style = AquaText.SmallLink,
            )
        } else {
            Text(
                text = "Podes pedir outro código dentro de ${contagem.segundosParaNovoPedido(agora)} s",
                style = AquaText.Footer.copy(color = MutedInk, fontSize = 11.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
