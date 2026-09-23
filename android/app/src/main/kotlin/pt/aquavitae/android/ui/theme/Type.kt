package pt.aquavitae.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pt.aquavitae.android.R

/**
 * Inter (licença SIL OFL 1.1), a fonte que os ecrãs de design usam. Os ficheiros vêm do Android Studio
 * (que a traz consigo). Antes de publicar a app, acrescentar o aviso de licença da Inter numa página de
 * "licenças de código aberto".
 */
val InterFamily = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private val base = Typography()

// Todos os estilos do Material 3 passam a Inter (os tamanhos por omissão mantêm-se).
val AquaVitaeTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = InterFamily),
    displayMedium = base.displayMedium.copy(fontFamily = InterFamily),
    displaySmall = base.displaySmall.copy(fontFamily = InterFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = InterFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = InterFamily),
    headlineSmall = base.headlineSmall.copy(fontFamily = InterFamily),
    titleLarge = base.titleLarge.copy(fontFamily = InterFamily),
    titleMedium = base.titleMedium.copy(fontFamily = InterFamily),
    titleSmall = base.titleSmall.copy(fontFamily = InterFamily),
    bodyLarge = base.bodyLarge.copy(fontFamily = InterFamily),
    bodyMedium = base.bodyMedium.copy(fontFamily = InterFamily),
    bodySmall = base.bodySmall.copy(fontFamily = InterFamily),
    labelLarge = base.labelLarge.copy(fontFamily = InterFamily),
    labelMedium = base.labelMedium.copy(fontFamily = InterFamily),
    labelSmall = base.labelSmall.copy(fontFamily = InterFamily),
)

/** Estilos dos ecrãs de design (tamanhos medidos nos prints). */
object AquaText {
    /** Rótulo de um campo ("Username ou Email"): grená, negrito. */
    val Label = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Burgundy)

    /** O que o utilizador escreve num campo. */
    val Field = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, color = Ink)

    /** Texto dos botões grandes ("LOGIN", "COMEÇAR"). */
    val Button = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Paper)

    /** Ligações por baixo do cartão ("AINDA NÃO TENS CONTA? REGISTA-TE"). */
    val Link = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = Burgundy, letterSpacing = 0.2.sp)

    /** "Esqueci-me da password". */
    val SmallLink = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = Burgundy)

    /** "TERMOS E CONDIÇÕES" no fundo do ecrã. */
    val Footer = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, color = Burgundy, letterSpacing = 0.3.sp)

    /** Título dos cabeçalhos em pílula ("RECUPERAR PASSWORD") e dos popups. */
    val Title = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Paper)

    /** Mensagens de erro por baixo dos campos. */
    val Error = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = ErrorRed)

    /** Frase de apoio dentro do cartão ("Enviámos um código único para o email ..."). */
    val Hint = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Burgundy)

    /** A pergunta de cada ecrã de preferências ("QUE TIPOS DE BEBIDA PREFERES?") e "INSIRA O CÓDIGO ABAIXO". */
    val Question = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Burgundy)

    /** Opções das listas (tipos de bebida, castas) e rótulos dos sliders. */
    val Option = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Burgundy)

    /** "PRÓXIMO →" dentro do cartão. */
    val Next = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Paper)

    /** Os dígitos do código de recuperação. */
    val CodeDigit = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Ink)

    // Homepage (2026-09-23): os títulos de secção e o nome de uma bebida num cartão são serifados nos prints, diferente
    // do Inter do resto da app — aproximação com a serifa do sistema (falta saber qual é a exata do Figma).

    /** Título de secção ("Escolhido para ti", "As minhas Caves", "Estatísticas"). */
    val SectionSerif = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 20.sp, color = Ink)

    /** A parte a destaque de um título de secção ("Escolhido para **ti**"), na mesma serifa mas grená. */
    val SectionSerifAccent = SectionSerif.copy(color = Burgundy)

    /** "O que queres provar hoje?" — o maior texto do ecrã, serifado e itálico. */
    val GreetingSerif = TextStyle(
        fontFamily = FontFamily.Serif,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        fontSize = 22.sp,
        color = Ink,
        lineHeight = 27.sp,
    )

    /** O nome de uma bebida num cartão (serifado, a negrito). */
    val BebidaNomeSerif = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink)
}
