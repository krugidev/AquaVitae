package pt.aquavitae.api.compra.verificacao

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

// Só uma instância do backend deve ter isto ativo: com várias, cada uma correria o job à mesma hora
// (nesse caso convém trocar por um lock partilhado, p.ex. ShedLock, ou ativar só numa instância).
@Component
@ConditionalOnProperty(name = ["aquavitae.links.verificacao.ativo"], havingValue = "true", matchIfMissing = true)
class LinkVerificacaoScheduler(
    private val service: LinkVerificacaoService,
) {

    @Scheduled(cron = "\${aquavitae.links.verificacao.cron:0 0 4 * * *}", zone = "Europe/Lisbon")
    fun verificar() {
        service.executarAgendado()
    }
}
