package hackaton.fiapx.usecases

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.MailException
import org.springframework.test.util.ReflectionTestUtils
import kotlin.test.assertEquals
import kotlin.test.assertContentEquals

class SendEmailUseCaseTest {

    private val mailSender = mock(JavaMailSender::class.java)
    private lateinit var useCase: SendEmailUseCase

    @BeforeEach
    fun setup() {
        useCase = SendEmailUseCase(mailSender)
        // Use ReflectionTestUtils to set the @Value annotated field
        ReflectionTestUtils.setField(useCase, "sender", "test@fiapx.com")
    }

    @Test
    fun `sends email successfully with correct parameters`() {
        val recipient = "user@example.com"
        val subject = "Test Subject"
        val message = "Test message content"

        useCase.execute(recipient, subject, message)

        val messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        verify(mailSender).send(messageCaptor.capture())

        val capturedMessage = messageCaptor.value
        assertEquals("test@fiapx.com", capturedMessage.from)
        assertContentEquals(arrayOf(recipient), capturedMessage.to)
        assertEquals(subject, capturedMessage.subject)
        assertEquals(message, capturedMessage.text)
    }

    @Test
    fun `handles mail exception gracefully without throwing`() {
        val recipient = "user@example.com"
        val subject = "Test Subject"
        val message = "Test message"

        `when`(mailSender.send(any<SimpleMailMessage>())).thenThrow(object : MailException("SMTP server unavailable") {})

        assertDoesNotThrow {
            useCase.execute(recipient, subject, message)
        }

        verify(mailSender).send(any<SimpleMailMessage>())
    }

    @Test
    fun `sends email with empty subject`() {
        val recipient = "user@example.com"
        val emptySubject = ""
        val message = "Test message"

        useCase.execute(recipient, emptySubject, message)

        val messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        verify(mailSender).send(messageCaptor.capture())

        val capturedMessage = messageCaptor.value
        assertEquals("", capturedMessage.subject)
    }

    @Test
    fun `sends email with empty message`() {
        val recipient = "user@example.com"
        val subject = "Test Subject"
        val emptyMessage = ""

        useCase.execute(recipient, subject, emptyMessage)

        val messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        verify(mailSender).send(messageCaptor.capture())

        val capturedMessage = messageCaptor.value
        assertEquals("", capturedMessage.text)
    }

    @Test
    fun `sends email with special characters in content`() {
        val recipient = "user@example.com"
        val subject = "Subject with áéíóú ñ characters"
        val message = "Message with special chars: ç § ® © € £ ¥"

        useCase.execute(recipient, subject, message)

        val messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        verify(mailSender).send(messageCaptor.capture())

        val capturedMessage = messageCaptor.value
        assertEquals(subject, capturedMessage.subject)
        assertEquals(message, capturedMessage.text)
    }

    @Test
    fun `sends email with long content`() {
        val recipient = "user@example.com"
        val subject = "Long content test"
        val longMessage = "x".repeat(10000)

        useCase.execute(recipient, subject, longMessage)

        val messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        verify(mailSender).send(messageCaptor.capture())

        val capturedMessage = messageCaptor.value
        assertEquals(longMessage, capturedMessage.text)
    }

    @Test
    fun `sends email with multiline message`() {
        val recipient = "user@example.com"
        val subject = "Multiline test"
        val multilineMessage = "Line 1\nLine 2\nLine 3\n\nLine 5"

        useCase.execute(recipient, subject, multilineMessage)

        val messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        verify(mailSender).send(messageCaptor.capture())

        val capturedMessage = messageCaptor.value
        assertEquals(multilineMessage, capturedMessage.text)
    }

    @Test
    fun `handles connection timeout exception`() {
        val recipient = "user@example.com"
        val subject = "Test Subject"
        val message = "Test message"

        `when`(mailSender.send(any<SimpleMailMessage>())).thenThrow(object : MailException("Connection timeout") {})

        assertDoesNotThrow {
            useCase.execute(recipient, subject, message)
        }
    }

    @Test
    fun `handles authentication failure exception`() {
        val recipient = "user@example.com"
        val subject = "Test Subject"
        val message = "Test message"

        `when`(mailSender.send(any<SimpleMailMessage>())).thenThrow(object : MailException("Authentication failed") {})

        assertDoesNotThrow {
            useCase.execute(recipient, subject, message)
        }
    }

    @Test
    fun `sends multiple emails sequentially`() {
        val recipients = listOf("user1@example.com", "user2@example.com", "user3@example.com")
        val subject = "Batch email test"
        val message = "Test message"

        recipients.forEach { recipient ->
            useCase.execute(recipient, subject, message)
        }

        verify(mailSender, times(3)).send(any<SimpleMailMessage>())
    }

    @Test
    fun `preserves email format with html-like content`() {
        val recipient = "user@example.com"
        val subject = "HTML-like content"
        val message = "<p>This looks like HTML but should be sent as plain text</p>"

        useCase.execute(recipient, subject, message)

        val messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        verify(mailSender).send(messageCaptor.capture())

        val capturedMessage = messageCaptor.value
        assertEquals(message, capturedMessage.text)
    }
}
