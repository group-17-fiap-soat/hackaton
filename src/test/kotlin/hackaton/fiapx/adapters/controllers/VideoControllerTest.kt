package hackaton.fiapx.adapters.controllers

import hackaton.fiapx.adapters.presenters.VideoMapper
import hackaton.fiapx.commons.dto.response.VideoResponseV1
import hackaton.fiapx.entities.User
import hackaton.fiapx.entities.Video
import hackaton.fiapx.usecases.process.DownloadVideoUseCase
import hackaton.fiapx.usecases.process.ListVideoUseCase
import hackaton.fiapx.usecases.process.UploadVideoUseCase
import hackaton.fiapx.usecases.user.GetUserByEmailUseCase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.security.Principal
import java.util.*

class VideoControllerTest {

    private class FakePrincipal(private val name: String) : Principal {
        override fun getName(): String = name
    }

    private class FakeGetUserByEmailUseCase(private val returnUser: User?) : GetUserByEmailUseCase(
        object : hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface {
            override fun findById(id: UUID) = null
            override fun findByEmail(email: String) = returnUser
            override fun save(entity: User) = entity
        }
    ) {
        override fun execute(email: String): User? = returnUser
    }

    private class FakeUploadVideoUseCase(private val returnVideo: Video) : UploadVideoUseCase(
        object : hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface {
            override fun listAll() = emptyList<Video>()
            override fun save(entity: Video) = entity
        },
        object : hackaton.fiapx.usecases.process.ProcessVideoUseCase(
            object : hackaton.fiapx.usecases.process.CreateZipFileUseCase() {},
            object : hackaton.fiapx.usecases.SendEmailUseCase(
                object : org.springframework.mail.javamail.JavaMailSender {
                    override fun send(simpleMessage: org.springframework.mail.SimpleMailMessage) {}
                    override fun send(vararg simpleMessages: org.springframework.mail.SimpleMailMessage) {}
                    override fun createMimeMessage() = throw UnsupportedOperationException()
                    override fun createMimeMessage(contentStream: java.io.InputStream) = throw UnsupportedOperationException()
                    override fun send(mimeMessage: jakarta.mail.internet.MimeMessage) {}
                    override fun send(vararg mimeMessages: jakarta.mail.internet.MimeMessage) {}
                    override fun send(mimeMessagePreparator: org.springframework.mail.javamail.MimeMessagePreparator) {}
                    override fun send(vararg mimeMessagePreparators: org.springframework.mail.javamail.MimeMessagePreparator) {}
                }
            ) {}
        ) {}
    ) {
        override fun execute(user: User, videoFile: org.springframework.web.multipart.MultipartFile): Video = returnVideo
    }

    private class FakeListVideoUseCase(private val returnVideos: List<Video>) : ListVideoUseCase(
        object : hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface {
            override fun listAll() = returnVideos
            override fun save(entity: Video) = entity
        }
    ) {
        override fun execute(): List<Video> = returnVideos
    }

    private class FakeDownloadVideoUseCase(private val returnFile: File?) : DownloadVideoUseCase() {
        override fun execute(filename: String): File? = returnFile
    }

    @Test
    fun uploadReturnsCreatedWhenVideoProcessedSuccessfully() {
        val user = User(UUID.randomUUID(), "John", "john@test.com", "password")
        val video = Video(
            id = UUID.randomUUID(),
            userId = user.id,
            originalVideoPath = "path/to/video.mp4",
            fileSize = 1000L,
            status = hackaton.fiapx.commons.enums.VideoProcessStatusEnum.SUCCESS
        )
        val controller = VideoController(
            FakeUploadVideoUseCase(video),
            FakeListVideoUseCase(emptyList()),
            FakeDownloadVideoUseCase(null),
            FakeGetUserByEmailUseCase(user)
        )
        val principal = FakePrincipal("john@test.com")
        val file = MockMultipartFile("video", "test.mp4", "video/mp4", "content".toByteArray())

        val response = controller.upload(principal, file)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        org.junit.jupiter.api.Assertions.assertNotNull(response.body)
        assertEquals(video.id, response.body?.id)
    }

    @Test
    fun uploadThrowsRuntimeExceptionWhenUserNotFound() {
        val controller = VideoController(
            FakeUploadVideoUseCase(Video()),
            FakeListVideoUseCase(emptyList()),
            FakeDownloadVideoUseCase(null),
            FakeGetUserByEmailUseCase(null)
        )
        val principal = FakePrincipal("notfound@test.com")
        val file = MockMultipartFile("video", "test.mp4", "video/mp4", "content".toByteArray())

        val exception = assertThrows(RuntimeException::class.java) {
            controller.upload(principal, file)
        }

        assertTrue(exception.message!!.contains("Usuário não encontrado no sistema"))
    }

    @Test
    fun statusReturnsOkWithVideoList() {
        val video1 = Video(UUID.randomUUID(), UUID.randomUUID(), "path1.mp4")
        val video2 = Video(UUID.randomUUID(), UUID.randomUUID(), "path2.mp4")
        val videos = listOf(video1, video2)
        val controller = VideoController(
            FakeUploadVideoUseCase(Video()),
            FakeListVideoUseCase(videos),
            FakeDownloadVideoUseCase(null),
            FakeGetUserByEmailUseCase(null)
        )

        val response = controller.status()

        assertEquals(HttpStatus.OK, response.statusCode)
        org.junit.jupiter.api.Assertions.assertNotNull(response.body)
        assertEquals(2, response.body?.size)
    }

    @Test
    fun statusReturnsOkWithEmptyListWhenNoVideos() {
        val controller = VideoController(
            FakeUploadVideoUseCase(Video()),
            FakeListVideoUseCase(emptyList()),
            FakeDownloadVideoUseCase(null),
            FakeGetUserByEmailUseCase(null)
        )

        val response = controller.status()

        assertEquals(HttpStatus.OK, response.statusCode)
        org.junit.jupiter.api.Assertions.assertNotNull(response.body)
        assertTrue(response.body!!.isEmpty())
    }

    @Test
    fun downloadReturnsFileWhenFileExists() {
        val tempFile = File.createTempFile("test", ".zip")
        tempFile.writeText("test content")
        tempFile.deleteOnExit()
        val controller = VideoController(
            FakeUploadVideoUseCase(Video()),
            FakeListVideoUseCase(emptyList()),
            FakeDownloadVideoUseCase(tempFile),
            FakeGetUserByEmailUseCase(null)
        )

        val response = controller.download("test.zip")

        assertEquals(HttpStatus.OK, response.statusCode)
        org.junit.jupiter.api.Assertions.assertNotNull(response.body)
        assertTrue(response.headers.containsKey(HttpHeaders.CONTENT_DISPOSITION))
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.headers.contentType)
        assertEquals(tempFile.length(), response.headers.contentLength)
    }

    @Test
    fun downloadReturnsNotFoundWhenFileDoesNotExist() {
        val controller = VideoController(
            FakeUploadVideoUseCase(Video()),
            FakeListVideoUseCase(emptyList()),
            FakeDownloadVideoUseCase(null),
            FakeGetUserByEmailUseCase(null)
        )

        val response = controller.download("nonexistent.zip")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        org.junit.jupiter.api.Assertions.assertNull(response.body)
    }

    @Test
    fun downloadSetsCorrectHeadersForFileDownload() {
        val tempFile = File.createTempFile("myfile", ".zip")
        tempFile.writeText("content")
        tempFile.deleteOnExit()
        val controller = VideoController(
            FakeUploadVideoUseCase(Video()),
            FakeListVideoUseCase(emptyList()),
            FakeDownloadVideoUseCase(tempFile),
            FakeGetUserByEmailUseCase(null)
        )

        val response = controller.download("myfile.zip")

        assertEquals(HttpStatus.OK, response.statusCode)
        val contentDisposition = response.headers.getFirst(HttpHeaders.CONTENT_DISPOSITION)
        assertTrue(contentDisposition!!.contains("attachment"))
        assertTrue(contentDisposition.contains("filename"))
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.headers.contentType)
        assertTrue(response.headers.contentLength > 0)
    }
}
