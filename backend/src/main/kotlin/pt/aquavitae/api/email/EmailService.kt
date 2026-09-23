package pt.aquavitae.api.email

import jakarta.mail.internet.InternetAddress
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

/**
 * Envia os emails da API por SMTP. O servidor vem da configuração (`SPRING_MAIL_HOST`, ver `application.yml`): sem ele não há
 * [JavaMailSender] e o envio fica em log — só em dev, onde o código de recuperação aparece no log da API para se poder testar
 * sem caixa de email. Fora de dev nunca se põe o código em log (seria uma fuga): avisa-se que o email não está configurado.
 */
@Service
class EmailService(
    private val mailSender: ObjectProvider<JavaMailSender>,
    private val environment: Environment,
    @Value("\${aquavitae.mail.from-address}") private val remetenteEndereco: String,
    @Value("\${aquavitae.mail.from-name}") private val remetenteNome: String,
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    /**
     * Envia o código de recuperação de password. Corre noutra thread (`@Async`): o pedido HTTP não espera pelo servidor de
     * email, e o tempo de resposta não diz se a conta existe (a API responde igual exista ou não). Uma falha só vai para o log;
     * o utilizador pode pedir outro código passado o intervalo mínimo.
     */
    @Async
    fun enviarCodigoRecuperacao(utilizadorId: Long, destinatario: String, nome: String?, codigo: String, validadeMinutos: Long) {
        val sender = mailSender.ifAvailable
        if (sender == null) {
            semServidor(utilizadorId, codigo)
            return
        }
        try {
            val email = RecuperacaoPasswordEmail.compor(nome, codigo, validadeMinutos)
            val mensagem = sender.createMimeMessage()
            MimeMessageHelper(mensagem, true, "UTF-8").apply {
                setFrom(InternetAddress(remetenteEndereco, remetenteNome, "UTF-8"))
                setTo(destinatario)
                setSubject(email.assunto)
                setText(email.texto, email.html)
            }
            sender.send(mensagem)
            logger.info("Email de recuperação de password enviado (utilizador {})", utilizadorId)
        } catch (e: Exception) {
            logger.error("Falha ao enviar o email de recuperação de password (utilizador {}): {}", utilizadorId, e.message)
        }
    }

    private fun semServidor(utilizadorId: Long, codigo: String) {
        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            logger.info(
                "Email não configurado (SPRING_MAIL_HOST): código de recuperação de password do utilizador {}: {}",
                utilizadorId, codigo,
            )
        } else {
            logger.warn("Email não configurado (SPRING_MAIL_HOST): não se enviou o código de recuperação (utilizador {})", utilizadorId)
        }
    }
}
