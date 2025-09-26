package hackaton.fiapx.usecases.process

import hackaton.fiapx.commons.dto.response.VideoProcessResponseV1
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.entities.User
import hackaton.fiapx.usecases.SendEmailUseCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.springframework.mail.MailSendException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessagePreparator
import java.io.File
import java.io.InputStream
import java.io.IOException
import jakarta.mail.internet.MimeMessage

class ProcessVideoUseCaseTest {

    private class StubMailSender(private val throwEx: Boolean = false) : JavaMailSender {
        var last: SimpleMailMessage? = null
        override fun send(simpleMessage: SimpleMailMessage) {
            if (throwEx) throw MailSendException("x")
            last = simpleMessage
        }
        override fun send(vararg simpleMessages: SimpleMailMessage) { throw UnsupportedOperationException() }
        override fun createMimeMessage(): MimeMessage { throw UnsupportedOperationException() }
        override fun createMimeMessage(contentStream: InputStream): MimeMessage { throw UnsupportedOperationException() }
        override fun send(mimeMessage: MimeMessage) { throw UnsupportedOperationException() }
        override fun send(vararg mimeMessages: MimeMessage) { throw UnsupportedOperationException() }
        override fun send(mimeMessagePreparator: MimeMessagePreparator) { throw UnsupportedOperationException() }
        override fun send(vararg mimeMessagePreparators: MimeMessagePreparator) { throw UnsupportedOperationException() }
    }

    private fun newEmailUseCase(sender: JavaMailSender): SendEmailUseCase {
        val useCase = SendEmailUseCase(sender)
        val f = SendEmailUseCase::class.java.getDeclaredField("sender")
        f.isAccessible = true
        f.set(useCase, "noreply@example.com")
        return useCase
    }

    @BeforeEach
    fun setupDirs() {
        File("outputs").mkdirs()
        File("temp").mkdirs()
    }

    @AfterEach
    fun cleanup() {
        File("ffmpeg.cmd").takeIf { it.exists() }?.delete()
        File("outputs").deleteRecursively()
        File("temp").deleteRecursively()
    }

    @Test
    fun throwsIOExceptionWhenFfmpegIsNotAvailable() {
        val useCase = ProcessVideoUseCase(CreateZipFileUseCase(), newEmailUseCase(StubMailSender()))
        assertThrows(IOException::class.java) {
            useCase.execute(User(email = "u@x.com"), "dummy.mp4", "ts1")
        }
    }

    @Disabled("Requires ffmpeg.cmd setup - enable manually for integration testing")
    @Test
    fun returnsErrorWhenFfmpegExitsWithNonZero() {
        File("ffmpeg.cmd").writeText("""
            @echo off
            exit /b 1
        """.trimIndent())

        val useCase = ProcessVideoUseCase(CreateZipFileUseCase(), newEmailUseCase(StubMailSender()))
        val result: VideoProcessResponseV1 = useCase.execute(User(email = "u@x.com"), "dummy.mp4", "ts1")

        assertEquals(VideoProcessStatusEnum.ERROR, result.status)
    }

    @Disabled("Requires ffmpeg.cmd setup - enable manually for integration testing")
    @Test
    fun returnsErrorAndSendsEmailWhenNoFramesGenerated() {
        File("ffmpeg.cmd").writeText("""
            @echo off
            exit /b 0
        """.trimIndent())

        val stubSender = StubMailSender()
        val useCase = ProcessVideoUseCase(CreateZipFileUseCase(), newEmailUseCase(stubSender))
        val result = useCase.execute(User(email = "user@x.com", name = "User"), "dummy.mp4", "ts2")

        assertEquals(VideoProcessStatusEnum.ERROR, result.status)
        org.junit.jupiter.api.Assertions.assertNotNull(stubSender.last)
        assertArrayEquals(arrayOf("user@x.com"), stubSender.last!!.to)
    }

    @Disabled("Requires ffmpeg.cmd setup - enable manually for integration testing")
    @Test
    fun returnsSuccessWhenFramesAreCreatedAndZipped() {
        File("ffmpeg.cmd").writeText("""
            @echo off
            setlocal EnableExtensions EnableDelayedExpansion
            set "dir="
            :loop
            if "%~1"=="" goto after
            set "dir=%~dp1"
            shift
            goto loop
            :after
            if not exist "!dir!" mkdir "!dir!"
            echo x> "!dir!frame_0001.png"
            echo x> "!dir!frame_0002.png"
            exit /b 0
        """.trimIndent())

        val useCase = ProcessVideoUseCase(CreateZipFileUseCase(), newEmailUseCase(StubMailSender()))
        val result = useCase.execute(User(email = "ok@x.com"), "video.mp4", "ts3")

        assertEquals(VideoProcessStatusEnum.SUCCESS, result.status)
        assertEquals(2, result.frameCount)
        org.junit.jupiter.api.Assertions.assertNotNull(result.zipPath)
        assertTrue(File("outputs", result.zipPath!!).exists())
    }
}
