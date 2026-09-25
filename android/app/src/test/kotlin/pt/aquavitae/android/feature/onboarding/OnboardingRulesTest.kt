package pt.aquavitae.android.feature.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import pt.aquavitae.android.data.model.PreferenciaRequest
import pt.aquavitae.android.data.model.UtilizadorUpdateRequest

class OnboardingRulesTest {

    // --- A ordem dos ecrãs ---

    @Test
    fun `sem vinho o fluxo acaba na acidez`() {
        assertEquals(OnboardingStep.Nacionalidade, nextStep(OnboardingStep.Nome, escolheuVinho = false))
        assertEquals(OnboardingStep.Acidez, nextStep(OnboardingStep.Docura, escolheuVinho = false))
        assertNull(nextStep(OnboardingStep.Acidez, escolheuVinho = false))
    }

    @Test
    fun `com vinho a acidez segue para as castas e as castas sao o fim`() {
        assertEquals(OnboardingStep.Castas, nextStep(OnboardingStep.Acidez, escolheuVinho = true))
        assertNull(nextStep(OnboardingStep.Castas, escolheuVinho = true))
    }

    @Test
    fun `voltar percorre os ecras ao contrario e o primeiro nao tem anterior`() {
        assertNull(previousStep(OnboardingStep.Nome))
        assertEquals(OnboardingStep.Acidez, previousStep(OnboardingStep.Castas))
        // Ir e voltar em todos os ecrãs (com vinho, que é o caminho mais comprido) dá sempre o mesmo ecrã.
        var passo: OnboardingStep? = OnboardingStep.Nome
        while (passo != null) {
            val seguinte = nextStep(passo, escolheuVinho = true) ?: break
            assertEquals(passo, previousStep(seguinte))
            passo = seguinte
        }
    }

    // --- O pedido do perfil ---

    @Test
    fun `sem nada preenchido nao ha pedido de perfil`() {
        assertNull(buildPerfil("", "  ", null, "", null))
    }

    @Test
    fun `o perfil leva so o que foi preenchido e sem espacos nas pontas`() {
        val pedido = buildPerfil(" Ana ", "", 1L, "  gosto de tintos  ", null)
        assertEquals(UtilizadorUpdateRequest(firstName = "Ana", lastName = null, nationalityId = 1L, bioDesc = "gosto de tintos", avatarId = null), pedido)
    }

    @Test
    fun `so o avatar ja chega para haver pedido de perfil`() {
        assertNotNull(buildPerfil("", "", null, "", 21L))
    }

    // --- O pedido das preferências ---

    private val vinho = 1L
    private val whisky = 2L

    @Test
    fun `se ignorou tudo nao ha pedido de preferencias`() {
        assertNull(buildPreferencias(emptySet(), null, null, emptySet(), vinho))
    }

    @Test
    fun `um nivel de docura vai como intervalo min igual a max`() {
        val pedido = buildPreferencias(emptySet(), docura = 4, acidez = null, castaIds = emptySet(), vinhoId = vinho)
        assertEquals(PreferenciaRequest(docuraMin = 4, docuraMax = 4), pedido)
    }

    @Test
    fun `so a acidez respondida tambem conta`() {
        val pedido = buildPreferencias(emptySet(), docura = null, acidez = 2, castaIds = emptySet(), vinhoId = vinho)
        assertEquals(PreferenciaRequest(acidezMin = 2, acidezMax = 2), pedido)
    }

    @Test
    fun `as castas so entram se escolheu vinho`() {
        val comVinho = buildPreferencias(setOf(vinho, whisky), null, null, setOf(30L, 10L), vinho)
        assertEquals(listOf(10L, 30L), comVinho?.castaIds)
        assertEquals(listOf(vinho, whisky), comVinho?.categoriaIds)

        // Marcou castas e depois desmarcou o vinho: as castas ficam de fora.
        val semVinho = buildPreferencias(setOf(whisky), null, null, setOf(10L), vinho)
        assertEquals(emptyList<Long>(), semVinho?.castaIds)
        assertEquals(listOf(whisky), semVinho?.categoriaIds)
    }

    @Test
    fun `castas sem nenhuma categoria escolhida nao geram pedido`() {
        assertNull(buildPreferencias(emptySet(), null, null, setOf(10L), vinho))
    }

    @Test
    fun `sem o id do vinho as castas nao entram`() {
        assertEquals(emptyList<Long>(), buildPreferencias(setOf(whisky), null, null, setOf(10L), vinhoId = null)?.castaIds)
    }
}
