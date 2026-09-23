package pt.aquavitae.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

// @EnableScheduling: o job diário de verificação dos links de compra. @EnableAsync: métodos @Async, hoje só o envio de emails
// (para o pedido HTTP não esperar pelo servidor SMTP).
@Configuration
@EnableScheduling
@EnableAsync
class SchedulingConfig
