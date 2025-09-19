package hackaton.fiapx.usecases

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class SendEmailUseCase(
    private val mailSender: JavaMailSender,
    @param:Value("\${spring.mail.username}")
    private val sender: String
) {

    fun execute(recipient: String, subject: String, message: String) {
        try {
            val simpleMailMessage = SimpleMailMessage().apply {
                setFrom(sender)
                setTo(recipient)
                setSubject(subject)
                setText(message)

            }

            println("E-mail enviado!")
            mailSender.send(simpleMailMessage)
        } catch (e: MailException) {
            println("Erro ao enviar e-mail: ${e.message}")
        }
    }

}