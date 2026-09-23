package pt.aquavitae.android.feature.onboarding

import pt.aquavitae.android.data.model.PreferenciaRequest
import pt.aquavitae.android.data.model.UtilizadorUpdateRequest

/** Os ecrãs do onboarding, pela ordem em que aparecem. As castas só aparecem a quem escolheu vinho. */
enum class OnboardingStep { Nome, Nacionalidade, Avatar, Tipos, Docura, Acidez, Castas }

// Tamanhos máximos da API (as colunas de `utilizador`): nome 25, apelido 40, descrição 1000.
const val NOME_MAX = 25
const val APELIDO_MAX = 40
const val DESCRICAO_MAX = 1000

/** Quantas castas a lista mostra de cada vez; ao chegar ao fim aparecem mais tantas. */
const val CASTAS_POR_PAGINA = 10

/** O nome da categoria de bebida que abre o ecrã das castas (comparado sem distinguir maiúsculas). */
const val CATEGORIA_VINHO = "Vinho"

/** Os níveis dos sliders, de cima (nível 1) para baixo (nível 5). */
val NIVEIS_DOCURA = listOf("Muito seca", "Seca", "Equilibrada", "Doce", "Muito doce")
val NIVEIS_ACIDEZ = listOf("Muito macia", "Macia", "Equilibrada", "Fresca", "Muito fresca")

/** O passo a seguir, ou `null` se `current` é o último (então guarda-se tudo). As castas só vêm a seguir à acidez se escolheu vinho. */
fun nextStep(current: OnboardingStep, escolheuVinho: Boolean): OnboardingStep? = when (current) {
    OnboardingStep.Nome -> OnboardingStep.Nacionalidade
    OnboardingStep.Nacionalidade -> OnboardingStep.Avatar
    OnboardingStep.Avatar -> OnboardingStep.Tipos
    OnboardingStep.Tipos -> OnboardingStep.Docura
    OnboardingStep.Docura -> OnboardingStep.Acidez
    OnboardingStep.Acidez -> if (escolheuVinho) OnboardingStep.Castas else null
    OnboardingStep.Castas -> null
}

/** O passo anterior, ou `null` no primeiro. */
fun previousStep(current: OnboardingStep): OnboardingStep? = when (current) {
    OnboardingStep.Nome -> null
    OnboardingStep.Nacionalidade -> OnboardingStep.Nome
    OnboardingStep.Avatar -> OnboardingStep.Nacionalidade
    OnboardingStep.Tipos -> OnboardingStep.Avatar
    OnboardingStep.Docura -> OnboardingStep.Tipos
    OnboardingStep.Acidez -> OnboardingStep.Docura
    OnboardingStep.Castas -> OnboardingStep.Acidez
}

/**
 * O pedido `PUT /api/users/me` com o que o utilizador preencheu (textos sem espaços nas pontas; vazio conta como não
 * preenchido). `null` se não preencheu nada: nesse caso não se faz pedido nenhum.
 */
fun buildPerfil(
    firstName: String,
    lastName: String,
    nationalityId: Long?,
    bioDesc: String,
    avatarId: Long?,
): UtilizadorUpdateRequest? {
    val pedido = UtilizadorUpdateRequest(
        firstName = firstName.trim().ifEmpty { null },
        lastName = lastName.trim().ifEmpty { null },
        nationalityId = nationalityId,
        bioDesc = bioDesc.trim().ifEmpty { null },
        avatarId = avatarId,
    )
    return pedido.takeUnless { it == UtilizadorUpdateRequest() }
}

/**
 * O pedido `PUT /api/users/me/preferencias` com o que o utilizador respondeu, ou `null` se ignorou tudo (ignorar não entra
 * nas preferências). Doçura e acidez são um nível só, enviado como intervalo `min = max`. As castas só entram se
 * escolheu vinho (`vinhoId` é o id da categoria "Vinho"): quem marcou castas e depois desmarcou o vinho não as leva.
 */
fun buildPreferencias(
    categoriaIds: Set<Long>,
    docura: Int?,
    acidez: Int?,
    castaIds: Set<Long>,
    vinhoId: Long?,
): PreferenciaRequest? {
    val escolheuVinho = vinhoId != null && vinhoId in categoriaIds
    val pedido = PreferenciaRequest(
        docuraMin = docura,
        docuraMax = docura,
        acidezMin = acidez,
        acidezMax = acidez,
        categoriaIds = categoriaIds.sorted(),
        castaIds = if (escolheuVinho) castaIds.sorted() else emptyList(),
    )
    return pedido.takeUnless { it == PreferenciaRequest() }
}
