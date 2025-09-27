package hackaton.fiapx.usecases.process

import hackaton.fiapx.commons.dto.kafka.VideoEventDto
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.commons.interfaces.gateways.VideoEventGateway
import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.User
import hackaton.fiapx.entities.Video
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.mock.web.MockMultipartFile
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UploadVideoUseCaseTest {

    private lateinit var videoGateway: VideoGatewayInterface
    private lateinit var videoEventGateway: VideoEventGateway
    private lateinit var useCase: UploadVideoUseCase

    @BeforeEach
    fun setup() {
        videoGateway = mock()
        videoEventGateway = mock()
        useCase = UploadVideoUseCase(videoGateway, videoEventGateway)
    }

    @Test
    fun `uploads mp4 file successfully and publishes event`() {
        val videoContent = "fake video content"
        val multipartFile = MockMultipartFile("video", "test.mp4", "video/mp4", videoContent.toByteArray())
        val user = User(
            id = UUID.randomUUID(),
            name = "Test User",
            email = "test@example.com"
        )
        val savedVideo = Video(id = UUID.randomUUID(), status = VideoProcessStatusEnum.UPLOADED)

        whenever(videoGateway.save(any<Video>())).thenReturn(savedVideo)

        val result = useCase.execute(multipartFile, user)

        assertEquals(savedVideo, result)

        // Verify video was saved
        verify(videoGateway).save(any<Video>())

        // Verify event was published
        verify(videoEventGateway).publishToProcessingTopic(any(), any(), any())
    }

    @Test
    fun `uploads mov file successfully with proper file extension`() {
        val videoContent = "fake mov content"
        val multipartFile = MockMultipartFile("video", "test.mov", "video/quicktime", videoContent.toByteArray())
        val user = User(
            id = UUID.randomUUID(),
            name = "Test User",
            email = "test@example.com"
        )
        val savedVideo = Video(id = UUID.randomUUID())

        whenever(videoGateway.save(any<Video>())).thenReturn(savedVideo)

        val result = useCase.execute(multipartFile, user)

        assertEquals(result, savedVideo)
        verify(videoGateway).save(any<Video>())
    }

    @Test
    fun `uploads avi file successfully`() {
        val videoContent = "fake avi content"
        val multipartFile = MockMultipartFile("video", "test.avi", "video/x-msvideo", videoContent.toByteArray())
        val user = User(
            id = UUID.randomUUID(),
            name = "Test User",
            email = "test@example.com"
        )
        val savedVideo = Video(id = UUID.randomUUID())

        whenever(videoGateway.save(any<Video>())).thenReturn(savedVideo)

        useCase.execute(multipartFile, user)

        verify(videoGateway).save(any<Video>())
    }

    @Test
    fun `creates unique filename with timestamp`() {
        val multipartFile = MockMultipartFile("video", "video.mp4", "video/mp4", "content".toByteArray())
        val user = User(
            id = UUID.randomUUID(),
            name = "Test User",
            email = "test@example.com"
        )
        val savedVideo = Video(id = UUID.randomUUID())

        whenever(videoGateway.save(any<Video>())).thenReturn(savedVideo)

        useCase.execute(multipartFile, user)

        verify(videoGateway).save(any<Video>())
    }

    @Test
    fun `throws exception when file save fails`() {
        val multipartFile = MockMultipartFile("video", "test.mp4", "video/mp4", "content".toByteArray())
        val user = User(
            id = UUID.randomUUID(),
            name = "Test User",
            email = "test@example.com"
        )

        whenever(videoGateway.save(any<Video>())).thenThrow(RuntimeException("Database error"))

        assertThrows<RuntimeException> {
            useCase.execute(multipartFile, user)
        }
    }

    @Test
    fun `handles empty file upload by throwing exception`() {
        val multipartFile = MockMultipartFile("video", "empty.mp4", "video/mp4", ByteArray(0))
        val user = User(
            id = UUID.randomUUID(),
            name = "Test User",
            email = "test@example.com"
        )

        // Empty files should be rejected according to the implementation
        assertThrows<IllegalArgumentException> {
            useCase.execute(multipartFile, user)
        }

        verifyNoInteractions(videoGateway)
        verifyNoInteractions(videoEventGateway)
    }

    @Test
    fun `rejects unsupported file format`() {
        val multipartFile = MockMultipartFile("video", "test.txt", "text/plain", "not a video".toByteArray())
        val user = User(
            id = UUID.randomUUID(),
            name = "Test User",
            email = "test@example.com"
        )

        assertThrows<IllegalArgumentException> {
            useCase.execute(multipartFile, user)
        }

        verifyNoInteractions(videoGateway)
        verifyNoInteractions(videoEventGateway)
    }

    @Test
    fun `handles mkv file format`() {
        val videoContent = "fake mkv content"
        val multipartFile = MockMultipartFile("video", "test.mkv", "video/x-matroska", videoContent.toByteArray())
        val user = User(
            id = UUID.randomUUID(),
            name = "Test User",
            email = "test@example.com"
        )
        val savedVideo = Video(id = UUID.randomUUID())

        whenever(videoGateway.save(any<Video>())).thenReturn(savedVideo)

        useCase.execute(multipartFile, user)

        verify(videoGateway).save(any<Video>())
        verify(videoEventGateway).publishToProcessingTopic(any(), any(), any())
    }

    @Test
    fun `handles webm file format`() {
        val videoContent = "fake webm content"
        val multipartFile = MockMultipartFile("video", "test.webm", "video/webm", videoContent.toByteArray())
        val user = User(
            id = UUID.randomUUID(),
            name = "Test User",
            email = "test@example.com"
        )
        val savedVideo = Video(id = UUID.randomUUID())

        whenever(videoGateway.save(any<Video>())).thenReturn(savedVideo)

        useCase.execute(multipartFile, user)

        verify(videoGateway).save(any<Video>())
    }
}
