package pt.aquavitae.android.feature.legal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import pt.aquavitae.android.data.model.LookupState
import pt.aquavitae.android.data.model.TermosTexto
import pt.aquavitae.android.ui.components.DialogFillScreen
import pt.aquavitae.android.ui.components.DialogScrim
import pt.aquavitae.android.ui.components.LoadingDots
import pt.aquavitae.android.ui.theme.AquaText
import pt.aquavitae.android.ui.theme.Burgundy
import pt.aquavitae.android.ui.theme.ButtonShape
import pt.aquavitae.android.ui.theme.InterFamily
import kotlin.math.roundToInt

private val CorpoTexto = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp, color = Color.White)
private val TituloSecao = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp, color = Color.White)
private val FormaFolha = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

// Quanto é preciso arrastar o popup para baixo para ele se fechar.
private val LimiarArrasto = 90.dp

/**
 * O popup grená que sobe de baixo com o texto dos termos e condições (do login, do registo, da recuperação de password e do
 * "LER OS TERMOS" do popup de aceitação). O texto vem da API. Fecha-se com o botão, a deslizar o cabeçalho para baixo, ao tocar
 * fora ou com o gesto de voltar.
 *
 * Feito sobre um `Dialog` (e não sobre o `ModalBottomSheet` do Material 3, que nesta versão tem uma janela própria que não
 * desenha por baixo das barras do sistema e deixava a barra de estado e a de navegação por escurecer). Duas cenas exigiram
 * atenção: a janela do `Dialog` dimensiona-se por omissão ao conteúdo (`DialogFillScreen` força-a a ocupar o ecrã todo) e, com
 * `decorFitsSystemWindows = false`, o conteúdo desenha por baixo da barra de navegação do sistema — sem
 * `Modifier.navigationBarsPadding()` na caixa de fora, o botão "FECHAR" ficava tapado por essa barra.
 */
@Composable
fun TermsSheet(
    onDismiss: () -> Unit,
    viewModel: TermsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    // Lido AQUI, antes do Dialog: dentro dele é uma janela própria, que nem sempre recebe os insets da barra de navegação do
    // sistema corretamente (foi o que tapou o botão "FECHAR", com o texto a ir até ao fundo do ecrã sem o botão aparecer). +8dp
    // de folga: o valor lido aqui ficava uns pixels aquém do necessário para o botão não tocar a barra.
    val alturaBarraNavegacao = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
    var alturaPx by remember { mutableFloatStateOf(0f) }
    // Começa fora do ecrã (por baixo) e sobe assim que se sabe a altura do popup; ao fechar desce ao contrário.
    val deslocamento = remember { Animatable(0f) }
    var fechando by remember { mutableStateOf(false) }
    val limiarPx = with(LocalDensity.current) { LimiarArrasto.toPx() }

    fun fechar() {
        if (fechando) return
        fechando = true
        scope.launch {
            deslocamento.animateTo(alturaPx.coerceAtLeast(1f), tween(220))
            onDismiss()
        }
    }

    // Assim que a altura do popup é conhecida (1.º layout), desce-o para lá e sobe logo de seguida (a animação de entrada).
    LaunchedEffect(alturaPx) {
        if (alturaPx > 0f) {
            deslocamento.snapTo(alturaPx)
            deslocamento.animateTo(0f, tween(300))
        }
    }

    Dialog(
        onDismissRequest = ::fechar,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        DialogScrim()
        DialogFillScreen()
        // Tocar fora do popup fecha-o (a janela ocupa o ecrã todo, por isso "fora" é esta caixa). O padding de baixo (calculado
        // fora do Dialog, ver acima) garante que a barra de navegação nunca tapa o botão "FECHAR".
        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = alturaBarraNavegacao)
                .pointerInput(Unit) { detectTapGestures { fechar() } },
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, deslocamento.value.roundToInt()) }
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .onSizeChanged { alturaPx = it.height.toFloat() }
                    .clip(FormaFolha)
                    .background(Burgundy)
                    // Um toque dentro do popup não pode chegar à caixa de fora (que o fecharia).
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                // Cabeçalho: a pega e o título. É a parte que se arrasta para fechar.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(limiarPx) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (deslocamento.value > limiarPx) fechar() else scope.launch { deslocamento.animateTo(0f) }
                                },
                                onDragCancel = { scope.launch { deslocamento.animateTo(0f) } },
                            ) { change, dy ->
                                change.consume()
                                scope.launch { deslocamento.snapTo((deslocamento.value + dy).coerceAtLeast(0f)) }
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .padding(top = 12.dp, bottom = 14.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f)),
                    )
                    Text(
                        text = (state as? LookupState.Ready)?.data?.titulo ?: "Termos e Condições",
                        style = AquaText.Title,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                    )
                }

                Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp).padding(top = 10.dp)) {
                    when (val estado = state) {
                        LookupState.Loading -> LoadingDots(modifier = Modifier.align(Alignment.Center), color = Color.White)

                        is LookupState.Error -> Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(text = estado.message, style = CorpoTexto, textAlign = TextAlign.Center)
                            Text(
                                text = "TENTAR DE NOVO",
                                style = CorpoTexto.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, textDecoration = TextDecoration.Underline),
                                modifier = Modifier.clickable(onClick = viewModel::carregar).padding(vertical = 8.dp),
                            )
                        }

                        is LookupState.Ready -> TermosConteudo(estado.data)
                    }
                }

                Button(
                    onClick = ::fechar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 14.dp, bottom = 16.dp)
                        .height(52.dp),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Burgundy),
                ) {
                    Text("FECHAR", style = AquaText.Button.copy(fontSize = 16.sp, color = Burgundy))
                }
            }
        }
    }
}

/** Os blocos do texto: títulos de secção a negrito e parágrafos, a rolar. */
@Composable
private fun TermosConteudo(termos: TermosTexto) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        termos.blocos.forEachIndexed { indice, bloco ->
            if (bloco.isTitulo) {
                Text(text = bloco.texto, style = TituloSecao, modifier = Modifier.padding(top = if (indice == 0) 0.dp else 18.dp, bottom = 6.dp))
            } else {
                Text(text = bloco.texto, style = CorpoTexto, modifier = Modifier.padding(bottom = 10.dp))
            }
        }
    }
}
