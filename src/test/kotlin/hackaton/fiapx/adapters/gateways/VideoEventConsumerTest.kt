package hackaton.fiapx.adapters.gateways

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import hackaton.fiapx.commons.config.KafkaTopicConfig
import hackaton.fiapx.commons.dto.kafka.VideoEventDto
import hackaton.fiapx.commons.dto.response.VideoResponseV1
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.User
import hackaton.fiapx.entities.Video
import hackaton.fiapx.usecases.process.ProcessVideoUseCase
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.kafka.support.Acknowledgment
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals

class VideoEventConsumerTest {

    private lateinit var processVideoUseCase: ProcessVideoUseCase
    private lateinit var videoGateway: VideoGatewayInterface
    private lateinit var videoEventConsumer: VideoEventConsumer
    private lateinit var acknowledgment: Acknowledgment

    @BeforeEach
    fun setup() {
        processVideoUseCase = mock()
        videoGateway = mock()
        acknowledgment = mock()
        videoEventConsumer = VideoEventConsumer(processVideoUseCase, videoGateway)
    }

    @Test
    fun `handles video event successfully when video exists and processing succeeds`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val eventDto = VideoEventDto(
            videoId = videoId,
            userId = userId,
            userEmail = "user@example.com",
            userName = "Test User"
        )
        val video = Video(
            id = videoId,
            userId = userId,
            status = VideoProcessStatusEnum.UPLOADED,
            originalVideoPath = "test.mp4"
        )
        val processResult = VideoResponseV1(
            id = videoId,
            status = VideoProcessStatusEnum.FINISHED,
            zipPath = "frames.zip",
            frameCount = 120
        )

        val record = createConsumerRecord(eventDto)
        whenever(videoGateway.findById(videoId)).thenReturn(video)
        whenever(processVideoUseCase.execute(any<Video>(), any<User>())).thenReturn(processResult)
        whenever(videoGateway.save(any<Video>())).thenReturn(video)

        videoEventConsumer.handleVideoEvent(record, acknowledgment)

        verify(videoGateway).findById(videoId)
        verify(processVideoUseCase).execute(any<Video>(), any<User>())
        verify(videoGateway, times(2)).save(any<Video>())
        verify(acknowledgment).acknowledge()
    }



    @Test
    fun `handles video processing failure and updates status to error`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val eventDto = VideoEventDto(
            videoId = videoId,
            userId = userId,
            userEmail = "user@example.com",
            userName = "Test User"
        )
        val video = Video(
            id = videoId,
            userId = userId,
            status = VideoProcessStatusEnum.UPLOADED
        )

        val record = createConsumerRecord(eventDto)
        whenever(videoGateway.findById(videoId)).thenReturn(video)
        whenever(processVideoUseCase.execute(any<Video>(), any<User>())).thenThrow(RuntimeException("Processing failed"))
        whenever(videoGateway.save(any<Video>())).thenReturn(video)

        videoEventConsumer.handleVideoEvent(record, acknowledgment)

        val videoCaptor = argumentCaptor<Video>()
        verify(videoGateway, times(2)).save(videoCaptor.capture())

        val errorVideo = videoCaptor.allValues.last()
        assertEquals(VideoProcessStatusEnum.ERROR, errorVideo.status)
        assertEquals("Video processing failed: Processing failed", errorVideo.errorMessage)
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `handles null event gracefully`() {
        val record = mock<ConsumerRecord<String, Any>>()
        whenever(record.value()).thenReturn(null)
        whenever(record.partition()).thenReturn(0)
        whenever(record.offset()).thenReturn(123L)
        whenever(record.headers()).thenReturn(RecordHeaders())

        videoEventConsumer.handleVideoEvent(record, acknowledgment)

        verify(acknowledgment).acknowledge()
        verifyNoInteractions(videoGateway)
        verifyNoInteractions(processVideoUseCase)
    }

    @Test
    fun `handles invalid event format and acknowledges`() {
        val record = mock<ConsumerRecord<String, Any>>()
        whenever(record.value()).thenReturn("invalid-event-format")
        whenever(record.partition()).thenReturn(0)
        whenever(record.offset()).thenReturn(123L)
        whenever(record.headers()).thenReturn(RecordHeaders())

        videoEventConsumer.handleVideoEvent(record, acknowledgment)

        verify(acknowledgment).acknowledge()
        verifyNoInteractions(videoGateway)
        verifyNoInteractions(processVideoUseCase)
    }

    @Test
    fun `creates user object with correct event data`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val userName = "John Doe"
        val userEmail = "john@example.com"
        val eventDto = VideoEventDto(
            videoId = videoId,
            userId = userId,
            userEmail = userEmail,
            userName = userName
        )
        val video = Video(id = videoId, userId = userId, status = VideoProcessStatusEnum.UPLOADED)
        val processResult = VideoResponseV1(status = VideoProcessStatusEnum.FINISHED)

        val record = createConsumerRecord(eventDto)
        whenever(videoGateway.findById(videoId)).thenReturn(video)
        whenever(processVideoUseCase.execute(any<Video>(), any<User>())).thenReturn(processResult)
        whenever(videoGateway.save(any<Video>())).thenReturn(video)

        videoEventConsumer.handleVideoEvent(record, acknowledgment)

        val userCaptor = argumentCaptor<User>()
        verify(processVideoUseCase).execute(any<Video>(), userCaptor.capture())

        val capturedUser = userCaptor.firstValue
        assertEquals(userId, capturedUser.id)
        assertEquals(userName, capturedUser.name)
        assertEquals(userEmail, capturedUser.email)
    }

    @Test
    fun `updates video status to processing before execution`() {
        val videoId = UUID.randomUUID()
        val eventDto = VideoEventDto(
            videoId = videoId,
            userId = UUID.randomUUID(),
            userEmail = "user@example.com",
            userName = "Test User"
        )
        val video = Video(id = videoId, status = VideoProcessStatusEnum.UPLOADED)
        val processResult = VideoResponseV1(status = VideoProcessStatusEnum.FINISHED)

        val record = createConsumerRecord(eventDto)
        whenever(videoGateway.findById(videoId)).thenReturn(video)
        whenever(processVideoUseCase.execute(any<Video>(), any<User>())).thenReturn(processResult)
        whenever(videoGateway.save(any<Video>())).thenReturn(video)

        videoEventConsumer.handleVideoEvent(record, acknowledgment)

        val videoCaptor = argumentCaptor<Video>()
        verify(videoGateway, times(2)).save(videoCaptor.capture())

        val processingVideo = videoCaptor.firstValue
        assertEquals(VideoProcessStatusEnum.PROCESSING, processingVideo.status)
        assertEquals("Video processing started", processingVideo.errorMessage)
    }

    @Test
    fun `updates video with processing results on success`() {
        val videoId = UUID.randomUUID()
        val eventDto = VideoEventDto(
            videoId = videoId,
            userId = UUID.randomUUID(),
            userEmail = "user@example.com",
            userName = "Test User"
        )
        val video = Video(id = videoId, status = VideoProcessStatusEnum.UPLOADED)
        val processResult = VideoResponseV1(
            status = VideoProcessStatusEnum.FINISHED,
            zipPath = "result.zip",
            frameCount = 90
        )

        val record = createConsumerRecord(eventDto)
        whenever(videoGateway.findById(videoId)).thenReturn(video)
        whenever(processVideoUseCase.execute(any<Video>(), any<User>())).thenReturn(processResult)
        whenever(videoGateway.save(any<Video>())).thenReturn(video)

        videoEventConsumer.handleVideoEvent(record, acknowledgment)

        val videoCaptor = argumentCaptor<Video>()
        verify(videoGateway, times(2)).save(videoCaptor.capture())

        val finalVideo = videoCaptor.allValues.last()
        assertEquals(VideoProcessStatusEnum.FINISHED, finalVideo.status)
        assertEquals("result.zip", finalVideo.zipPath)
        assertEquals(90, finalVideo.frameCount)
    }

    @Test
    fun `handles processing result with error status`() {
        val videoId = UUID.randomUUID()
        val eventDto = VideoEventDto(
            videoId = videoId,
            userId = UUID.randomUUID(),
            userEmail = "user@example.com",
            userName = "Test User"
        )
        val video = Video(id = videoId, status = VideoProcessStatusEnum.UPLOADED)
        val processResult = VideoResponseV1(status = VideoProcessStatusEnum.ERROR)

        val record = createConsumerRecord(eventDto)
        whenever(videoGateway.findById(videoId)).thenReturn(video)
        whenever(processVideoUseCase.execute(any<Video>(), any<User>())).thenReturn(processResult)
        whenever(videoGateway.save(any<Video>())).thenReturn(video)

        videoEventConsumer.handleVideoEvent(record, acknowledgment)

        val videoCaptor = argumentCaptor<Video>()
        verify(videoGateway, times(2)).save(videoCaptor.capture())

        val finalVideo = videoCaptor.allValues.last()
        assertEquals(VideoProcessStatusEnum.ERROR, finalVideo.status)
        assertEquals("Video processing completed successfully", finalVideo.errorMessage)
    }

    @Test
    fun `continues processing when video status update fails`() {
        val videoId = UUID.randomUUID()
        val eventDto = VideoEventDto(
            videoId = videoId,
            userId = UUID.randomUUID(),
            userEmail = "user@example.com",
            userName = "Test User"
        )
        val video = Video(id = videoId, status = VideoProcessStatusEnum.UPLOADED)
        val processResult = VideoResponseV1(status = VideoProcessStatusEnum.FINISHED)

        val record = createConsumerRecord(eventDto)
        whenever(videoGateway.findById(videoId)).thenReturn(video)
        whenever(videoGateway.save(any<Video>()))
            .thenThrow(RuntimeException("Database error"))
            .thenReturn(video)
        whenever(processVideoUseCase.execute(any<Video>(), any<User>())).thenReturn(processResult)

        videoEventConsumer.handleVideoEvent(record, acknowledgment)

        verify(processVideoUseCase).execute(any<Video>(), any<User>())
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `handles user with null name`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val eventDto = VideoEventDto(
            videoId = videoId,
            userId = userId,
            userEmail = "user@example.com",
            userName = null
        )
        val video = Video(id = videoId, status = VideoProcessStatusEnum.UPLOADED)
        val processResult = VideoResponseV1(status = VideoProcessStatusEnum.FINISHED)

        val record = createConsumerRecord(eventDto)
        whenever(videoGateway.findById(videoId)).thenReturn(video)
        whenever(processVideoUseCase.execute(any<Video>(), any<User>())).thenReturn(processResult)
        whenever(videoGateway.save(any<Video>())).thenReturn(video)

        videoEventConsumer.handleVideoEvent(record, acknowledgment)

        val userCaptor = argumentCaptor<User>()
        verify(processVideoUseCase).execute(any<Video>(), userCaptor.capture())

        val capturedUser = userCaptor.firstValue
        assertEquals(null, capturedUser.name)
        assertEquals(userId, capturedUser.id)
    }

    private fun createConsumerRecord(eventDto: VideoEventDto): ConsumerRecord<String, Any> {
        val record = mock<ConsumerRecord<String, Any>>()
        whenever(record.value()).thenReturn(eventDto)
        whenever(record.partition()).thenReturn(0)
        whenever(record.offset()).thenReturn(123L)
        whenever(record.headers()).thenReturn(RecordHeaders())
        return record
    }
}
