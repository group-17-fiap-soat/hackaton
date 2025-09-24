package hackaton.fiapx.usecases.process

import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.User
import hackaton.fiapx.entities.Video
import hackaton.fiapx.usecases.SendEmailUseCase
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.io.File
import java.io.IOException

class UploadVideoUseCaseTest {

    private class FakeVideoGateway : VideoGatewayInterface {
        var saved: Video? = null
        override fun listAll(): List<Video> = emptyList()
        override fun save(entity: Video): Video { saved = entity; return entity }
    }

    private class StubMailSender : JavaMailSender {
        override fun send(simpleMessage: SimpleMailMessage) {}
        override fun send(vararg simpleMessages: SimpleMailMessage) { throw UnsupportedOperationException() }
        override fun createMimeMessage() = throw UnsupportedOperationException()
        override fun createMimeMessage(contentStream: java.io.InputStream) = throw UnsupportedOperationException()
        override fun send(mimeMessage: jakarta.mail.internet.MimeMessage) = throw UnsupportedOperationException()
        override fun send(vararg mimeMessages: jakarta.mail.internet.MimeMessage) = throw UnsupportedOperationException()
        override fun send(mimeMessagePreparator: org.springframework.mail.javamail.MimeMessagePreparator) = throw UnsupportedOperationException()
        override fun send(vararg mimeMessagePreparators: org.springframework.mail.javamail.MimeMessagePreparator) = throw UnsupportedOperationException()
    }

    private fun newEmailUseCase(): SendEmailUseCase {
        val useCase = SendEmailUseCase(StubMailSender())
        val f = SendEmailUseCase::class.java.getDeclaredField("sender")
        f.isAccessible = true
        f.set(useCase, "noreply@example.com")
        return useCase
    }

    @BeforeEach
    fun setup() {
        File("uploads").mkdirs()
        File("outputs").mkdirs()
        File("temp").mkdirs()
    }

    @AfterEach
    fun cleanup() {
        File("ffmpeg.cmd").takeIf { it.exists() }?.delete()
        File("uploads").deleteRecursively()
        File("outputs").deleteRecursively()
        File("temp").deleteRecursively()
    }

    @Test
    fun throwsWhenFileIsEmpty() {
        val gateway = FakeVideoGateway()
        val process = ProcessVideoUseCase(CreateZipFileUseCase(), newEmailUseCase())
        val useCase = UploadVideoUseCase(gateway, process)

        val emptyFile = MockMultipartFile("video", "v.mp4", "video/mp4", ByteArray(0))

        val ex = assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(User(), emptyFile)
        }
        assertTrue(ex.message!!.contains("Erro ao receber arquivo"))
        org.junit.jupiter.api.Assertions.assertNull(gateway.saved)
    }

    @Test
    fun throwsWhenExtensionIsInvalid() {
        val gateway = FakeVideoGateway()
        val process = ProcessVideoUseCase(CreateZipFileUseCase(), newEmailUseCase())
        val useCase = UploadVideoUseCase(gateway, process)

        val badFile = MockMultipartFile("video", "notes.txt", "text/plain", "x".toByteArray())

        val ex = assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(User(), badFile)
        }
        assertTrue(ex.message!!.contains("Formato de arquivo não suportado"))
        org.junit.jupiter.api.Assertions.assertNull(gateway.saved)
    }

    @Test
    fun throwsIOExceptionWhenProcessingFails() {
        val gateway = FakeVideoGateway()
        val process = ProcessVideoUseCase(CreateZipFileUseCase(), newEmailUseCase())
        val useCase = UploadVideoUseCase(gateway, process)

        val file = MockMultipartFile("video", "clip.mp4", "video/mp4", ByteArray(5))

        assertThrows(IOException::class.java) {
            useCase.execute(User(), file)
        }
        org.junit.jupiter.api.Assertions.assertNull(gateway.saved)
    }

    @Disabled("Requires ffmpeg.cmd setup - enable manually for integration testing")
    @Test
    fun savesVideoAndReturnsSavedEntityWhenProcessingSucceeds() {
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

        val gateway = FakeVideoGateway()
        val process = ProcessVideoUseCase(CreateZipFileUseCase(), newEmailUseCase())
        val useCase = UploadVideoUseCase(gateway, process)

        val file = MockMultipartFile("video", "movie.mp4", "video/mp4", ByteArray(5))
        val user = User()

        val saved = useCase.execute(user, file)

        org.junit.jupiter.api.Assertions.assertNotNull(gateway.saved)
        assertEquals(saved, gateway.saved)
        assertEquals(VideoProcessStatusEnum.SUCCESS, saved.status)
        org.junit.jupiter.api.Assertions.assertNotNull(saved.zipPath)
        assertEquals(2, saved.frameCount)
        assertTrue(saved.originalVideoPath!!.contains("uploads"))
        assertTrue(saved.originalVideoPath!!.endsWith("movie.mp4"))
    }
}
