package pt.aquavitae.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

// "Hoje" na lógica de negócio (janela de consumo das caves, produtor da semana) é o de Portugal, não o do
// servidor — o mesmo fuso do job diário dos links (LinkVerificacaoScheduler). Injetável para os testes.
@Configuration
class TempoConfig {

    @Bean
    fun clock(): Clock = Clock.system(ZoneId.of("Europe/Lisbon"))
}
