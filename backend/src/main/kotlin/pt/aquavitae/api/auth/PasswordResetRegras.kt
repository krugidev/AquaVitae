package pt.aquavitae.api.auth

import java.time.Duration
import java.time.Instant

/**
 * Anti-abuso da recuperação de password: pode gerar-se (e enviar por email) um código novo, ou o pedido anterior da conta é
 * demasiado recente? Sem este intervalo mínimo, qualquer pessoa que soubesse um username podia encher a caixa de email de
 * outra. `ultimoPedidoEm` é `null` numa conta que nunca pediu nenhum. A API responde igual nos dois casos (não revela nada).
 */
fun podeGerarNovoCodigo(ultimoPedidoEm: Instant?, agora: Instant, intervaloMinimoSegundos: Long): Boolean =
    ultimoPedidoEm == null || Duration.between(ultimoPedidoEm, agora).seconds >= intervaloMinimoSegundos
