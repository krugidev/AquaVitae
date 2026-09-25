package pt.aquavitae.api.utilizador

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Termos e condições — regras puras, testáveis sem BD.
//
// Só se guarda QUANDO o utilizador aceitou (`utilizador_termos_aceites_em`). Precisa de (voltar a) aceitar se nunca aceitou
// ou se aceitou antes de os termos em vigor terem sido publicados (`aquavitae.termos.em-vigor-desde`; vazio = os termos
// ainda não mudaram, por isso só quem nunca aceitou precisa). Aceitar no instante exato da publicação conta como aceite.
fun precisaAceitarTermos(aceitesEm: Instant?, emVigorDesde: Instant?): Boolean =
    aceitesEm == null || (emVigorDesde != null && aceitesEm < emVigorDesde)

// "2026-10-01" -> o início desse dia em Portugal. Vazio ou em branco = sem data. Uma data mal escrita rebenta ao arrancar
// (melhor do que ignorá-la em silêncio e deixar de pedir a nova aceitação).
fun emVigorDesdeInstant(valor: String, zona: ZoneId): Instant? =
    valor.trim().takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it).atStartOfDay(zona).toInstant() }
