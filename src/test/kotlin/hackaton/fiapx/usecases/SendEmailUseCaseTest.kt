package hackaton.fiapx.usecases

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.mail.MailSendException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class SendEmailUseCaseTest {

    private class CapturingMailSender(private val throwEx: Boolean = false) : JavaMailSender {
        var last: SimpleMailMessage? = null
        override fun send(simpleMessage: SimpleMailMessage) {
            if (throwEx) throw MailSendException("x")
            last = simpleMessage
        }
        override fun send(vararg simpleMessages: SimpleMailMessage) { throw UnsupportedOperationException() }
        override fun createMimeMessage() = throw UnsupportedOperationException()
        override fun createMimeMessage(contentStream: java.io.InputStream) = throw UnsupportedOperationException()
        override fun send(mimeMessage: jakarta.mail.internet.MimeMessage) = throw UnsupportedOperationException()
        override fun send(vararg mimeMessages: jakarta.mail.internet.MimeMessage) = throw UnsupportedOperationException()
        override fun send(mimeMessagePreparator: org.springframework.mail.javamail.MimeMessagePreparator) = throw UnsupportedOperationException()
        override fun send(vararg mimeMessagePreparators: org.springframework.mail.javamail.MimeMessagePreparator) = throw UnsupportedOperationException()
    }

    private fun newUseCase(sender: JavaMailSender, configuredFrom: String): SendEmailUseCase {
        val useCase = SendEmailUseCase(sender)
        val f = SendEmailUseCase::class.java.getDeclaredField("sender")
        f.isAccessible = true
        f.set(useCase, configuredFrom)
        return useCase
    }

    @Test
    fun sendsEmailWithConfiguredFromAddress() {
        val sender = CapturingMailSender()
        val useCase = newUseCase(sender, "noreply@x.com")

        useCase.execute("to@x.com", "subject", "body")

        val msg = sender.last
        org.junit.jupiter.api.Assertions.assertNotNull(msg)
        assertArrayEquals(arrayOf("to@x.com"), msg!!.to)
        assertEquals("noreply@x.com", msg.from)
        assertEquals("subject", msg.subject)
        assertEquals("body", msg.text)
    }

    @Test
    fun doesNotThrowWhenMailSenderFails() {
        val useCase = newUseCase(CapturingMailSender(true), "from@x.com")
        assertDoesNotThrow {
            useCase.execute("to@x.com", "s", "b")
        }
    }
}
