package hackaton.fiapx.adapters.controllers

import hackaton.fiapx.adapters.presenters.VideoMapper
import hackaton.fiapx.commons.config.jwt.JwtUserService
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.entities.User
import hackaton.fiapx.entities.Video
import hackaton.fiapx.usecases.process.DownloadVideoUseCase
import hackaton.fiapx.usecases.process.ListVideoUseCase
import hackaton.fiapx.usecases.process.UploadVideoUseCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class VideoControllerTest {

    private lateinit var uploadVideoUseCase: UploadVideoUseCase
    private lateinit var listVideoUseCase: ListVideoUseCase
    private lateinit var downloadVideoUseCase: DownloadVideoUseCase
    private lateinit var jwtUserService: JwtUserService
    private lateinit var videoController: VideoController

    @BeforeEach
    fun setup() {
        uploadVideoUseCase = mock()
        listVideoUseCase = mock()
        downloadVideoUseCase = mock()
        jwtUserService = mock()
        videoController = VideoController(
            uploadVideoUseCase,
            listVideoUseCase,
            downloadVideoUseCase,
            jwtUserService
        )
    }

    @Test
    fun `upload returns accepted status when video is uploaded successfully`() {
        val request = MockHttpServletRequest()
        val videoFile = MockMultipartFile("video", "test.mp4", "video/mp4", "content".toByteArray())
        val user = User(id = UUID.randomUUID(), name = "Test User", email = "test@example.com")
        val savedVideo = Video(
            id = UUID.randomUUID(),
            userId = user.id,
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = OffsetDateTime.now()
        )

        whenever(jwtUserService.getUserFromRequest(request)).thenReturn(user)
        whenever(uploadVideoUseCase.execute(videoFile, user)).thenReturn(savedVideo)

        val response = videoController.upload(request, videoFile)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(VideoProcessStatusEnum.UPLOADED, response.body!!.status)
        assertEquals("Video uploaded successfully and queued for processing", response.body!!.message)
    }

    @Test
    fun `upload returns bad request when user not found in token`() {
        val request = MockHttpServletRequest()
        val videoFile = MockMultipartFile("video", "test.mp4", "video/mp4", "content".toByteArray())

        whenever(jwtUserService.getUserFromRequest(request)).thenReturn(null)

        try {
            videoController.upload(request, videoFile)
        } catch (e: RuntimeException) {
            assertEquals("Usuário não encontrado no token JWT.", e.message)
        }
    }

    @Test
    fun `upload returns bad request when upload fails`() {
        val request = MockHttpServletRequest()
        val videoFile = MockMultipartFile("video", "test.mp4", "video/mp4", "content".toByteArray())
        val user = User(id = UUID.randomUUID(), name = "Test User", email = "test@example.com")

        whenever(jwtUserService.getUserFromRequest(request)).thenReturn(user)
        whenever(uploadVideoUseCase.execute(videoFile, user)).thenThrow(RuntimeException("Upload failed"))

        val response = videoController.upload(request, videoFile)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(VideoProcessStatusEnum.ERROR, response.body!!.status)
        assertEquals("Upload failed: Upload failed", response.body!!.message)
        assertNull(response.body!!.id)
    }

    @Test
    fun `upload handles empty video file gracefully`() {
        val request = MockHttpServletRequest()
        val videoFile = MockMultipartFile("video", "empty.mp4", "video/mp4", ByteArray(0))
        val user = User(id = UUID.randomUUID(), name = "Test User", email = "test@example.com")

        whenever(jwtUserService.getUserFromRequest(request)).thenReturn(user)
        whenever(uploadVideoUseCase.execute(videoFile, user)).thenThrow(IllegalArgumentException("Erro ao receber arquivo de vídeo."))

        val response = videoController.upload(request, videoFile)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Upload failed: Erro ao receber arquivo de vídeo.", response.body!!.message)
    }

    @Test
    fun `status returns ok with video list when user is authenticated`() {
        val request = MockHttpServletRequest()
        val user = User(id = UUID.randomUUID(), name = "Test User", email = "test@example.com")
        val videos = listOf(
            Video(id = UUID.randomUUID(), userId = user.id, status = VideoProcessStatusEnum.UPLOADED),
            Video(id = UUID.randomUUID(), userId = user.id, status = VideoProcessStatusEnum.FINISHED)
        )

        whenever(jwtUserService.getUserFromRequest(request)).thenReturn(user)
        whenever(listVideoUseCase.execute(user)).thenReturn(videos)

        val response = videoController.status(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body!!.size)
        assertEquals(videos[0].id, response.body!![0].id)
        assertEquals(videos[1].id, response.body!![1].id)
    }

    @Test
    fun `status returns empty list when user has no videos`() {
        val request = MockHttpServletRequest()
        val user = User(id = UUID.randomUUID(), name = "Test User", email = "test@example.com")

        whenever(jwtUserService.getUserFromRequest(request)).thenReturn(user)
        whenever(listVideoUseCase.execute(user)).thenReturn(emptyList())

        val response = videoController.status(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body!!.size)
    }

    @Test
    fun `status throws exception when user not found in token`() {
        val request = MockHttpServletRequest()

        whenever(jwtUserService.getUserFromRequest(request)).thenReturn(null)

        try {
            videoController.status(request)
        } catch (e: RuntimeException) {
            assertEquals("Usuário não encontrado no token JWT.", e.message)
        }
    }

    @Test
    fun `download returns ok with file resource when file exists`() {
        val filename = "test-video.mp4"
        val file = File("test-file.mp4")

        whenever(downloadVideoUseCase.execute(filename)).thenReturn(file)

        val response = videoController.download(filename)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("attachment; filename=\"${file.name}\"", response.headers.getFirst("Content-Disposition"))
    }

    @Test
    fun `download returns not found when file does not exist`() {
        val filename = "nonexistent.mp4"

        whenever(downloadVideoUseCase.execute(filename)).thenReturn(null)

        val response = videoController.download(filename)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `download handles filename with special characters`() {
        val filename = "test@#$%video.mp4"
        val file = File("processed-file.mp4")

        whenever(downloadVideoUseCase.execute(filename)).thenReturn(file)

        val response = videoController.download(filename)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `download sets correct content type and headers`() {
        val filename = "video.mp4"
        val file = File("output.mp4")

        whenever(downloadVideoUseCase.execute(filename)).thenReturn(file)

        val response = videoController.download(filename)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("application/octet-stream", response.headers.getFirst("Content-Type"))
        assertEquals("attachment; filename=\"${file.name}\"", response.headers.getFirst("Content-Disposition"))
    }
}
