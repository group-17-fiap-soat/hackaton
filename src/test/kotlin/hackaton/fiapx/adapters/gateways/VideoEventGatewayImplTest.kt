package hackaton.fiapx.adapters.gateways

import hackaton.fiapx.commons.config.KafkaTopicConfig
import hackaton.fiapx.commons.dto.kafka.VideoEventDto
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.kafka.support.KafkaHeaders
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

class VideoEventGatewayImplTest {

    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>
    private lateinit var videoEventGateway: VideoEventGatewayImpl

    @BeforeEach
    fun setup() {
        kafkaTemplate = mock()
        videoEventGateway = VideoEventGatewayImpl(kafkaTemplate)
    }

    @Test
    fun `publishToProcessingTopic sends event to correct topic with correct key`() {
        val videoId = UUID.randomUUID().toString()
        val event = VideoEventDto(
            videoId = UUID.fromString(videoId),
            userId = UUID.randomUUID(),
            userEmail = "test@example.com",
            userName = "Test User"
        )
        val eventType = "video upload"
        val expectedKey = "video-$videoId"

        val future = CompletableFuture<SendResult<String, Any>>()
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any())).thenReturn(future)

        videoEventGateway.publishToProcessingTopic(event, eventType, videoId)

        verify(kafkaTemplate).send(
            eq(KafkaTopicConfig.VIDEO_PROCESSING_TOPIC),
            eq(expectedKey),
            eq(event)
        )
    }

    @Test
    fun `publishToProcessingTopic handles successful publishing`() {
        val videoId = UUID.randomUUID().toString()
        val event = VideoEventDto(
            videoId = UUID.fromString(videoId),
            userId = UUID.randomUUID(),
            userEmail = "test@example.com",
            userName = "Test User"
        )
        val eventType = "video processing"

        val future = CompletableFuture<SendResult<String, Any>>()
        val sendResult = mock<SendResult<String, Any>>()
        val recordMetadata = mock<RecordMetadata>()

        whenever(sendResult.recordMetadata).thenReturn(recordMetadata)
        whenever(recordMetadata.partition()).thenReturn(0)
        whenever(recordMetadata.offset()).thenReturn(123L)
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any())).thenReturn(future)

        videoEventGateway.publishToProcessingTopic(event, eventType, videoId)

        future.complete(sendResult)

        verify(kafkaTemplate).send(
            eq(KafkaTopicConfig.VIDEO_PROCESSING_TOPIC),
            eq("video-$videoId"),
            eq(event)
        )
    }

    @Test
    fun `publishToProcessingTopic handles publishing failure and sends to DLQ`() {
        val videoId = UUID.randomUUID().toString()
        val event = VideoEventDto(
            videoId = UUID.fromString(videoId),
            userId = UUID.randomUUID(),
            userEmail = "user@example.com",
            userName = "User Name"
        )
        val eventType = "video processing"
        val exception = RuntimeException("Kafka connection failed")

        val future = CompletableFuture<SendResult<String, Any>>()
        val dlqFuture = CompletableFuture<SendResult<String, Any>>()

        whenever(kafkaTemplate.send(eq(KafkaTopicConfig.VIDEO_PROCESSING_TOPIC), any<String>(), any())).thenReturn(future)
        whenever(kafkaTemplate.send(eq(KafkaTopicConfig.VIDEO_PROCESSING_DLQ), any<String>(), any())).thenReturn(dlqFuture)

        videoEventGateway.publishToProcessingTopic(event, eventType, videoId)

        future.completeExceptionally(exception)

        verify(kafkaTemplate).send(
            eq(KafkaTopicConfig.VIDEO_PROCESSING_TOPIC),
            eq("video-$videoId"),
            eq(event)
        )
        verify(kafkaTemplate).send(
            eq(KafkaTopicConfig.VIDEO_PROCESSING_DLQ),
            eq("failure"),
            any<Map<String, Any>>()
        )
    }

    @Test
    fun `sendToDlq creates proper DLQ event with exception details`() {
        val originalEvent = VideoEventDto(
            videoId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            userEmail = "dlq@example.com",
            userName = "DLQ User"
        )
        val exception = IllegalArgumentException("Invalid video format")
        val dlqFuture = CompletableFuture<SendResult<String, Any>>()

        whenever(kafkaTemplate.send(eq(KafkaTopicConfig.VIDEO_PROCESSING_DLQ), any<String>(), any())).thenReturn(dlqFuture)

        videoEventGateway.sendToDlq(originalEvent, exception)

        val eventCaptor = argumentCaptor<Map<String, Any>>()
        verify(kafkaTemplate).send(
            eq(KafkaTopicConfig.VIDEO_PROCESSING_DLQ),
            eq("failure"),
            eventCaptor.capture()
        )

        val dlqEvent = eventCaptor.firstValue
        assertEquals(originalEvent, dlqEvent["originalEvent"])
        assertEquals("VideoEventDto", dlqEvent["eventType"])
        assertEquals("Invalid video format", dlqEvent["failureReason"])
        assertEquals(exception.stackTraceToString(), dlqEvent["stackTrace"])
    }

    @Test
    fun `sendToDlq handles DLQ sending failure gracefully`() {
        val originalEvent = VideoEventDto(
            videoId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            userEmail = "failure@example.com",
            userName = "Failure User"
        )
        val exception = RuntimeException("Original processing failed")

        whenever(kafkaTemplate.send(any<String>(), any<String>(), any())).thenThrow(RuntimeException("DLQ also failed"))

        videoEventGateway.sendToDlq(originalEvent, exception)

        verify(kafkaTemplate).send(
            eq(KafkaTopicConfig.VIDEO_PROCESSING_DLQ),
            eq("failure"),
            any<Map<String, Any>>()
        )
    }

    @Test
    fun `sendToDlq includes timestamp in DLQ event`() {
        val originalEvent = VideoEventDto(
            videoId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            userEmail = "timestamp@example.com",
            userName = "Timestamp User"
        )
        val exception = RuntimeException("Test exception")
        val dlqFuture = CompletableFuture<SendResult<String, Any>>()

        whenever(kafkaTemplate.send(any<String>(), any<String>(), any())).thenReturn(dlqFuture)

        val beforeTime = System.currentTimeMillis()
        videoEventGateway.sendToDlq(originalEvent, exception)
        val afterTime = System.currentTimeMillis()

        val eventCaptor = argumentCaptor<Map<String, Any>>()
        verify(kafkaTemplate).send(
            eq(KafkaTopicConfig.VIDEO_PROCESSING_DLQ),
            eq("failure"),
            eventCaptor.capture()
        )

        val timestamp = eventCaptor.firstValue["failureTimestamp"] as Long
        assert(timestamp >= beforeTime && timestamp <= afterTime)
    }

    @Test
    fun `publishToProcessingTopic handles different event types`() {
        val videoId = UUID.randomUUID().toString()
        val events = listOf(
            Pair(VideoEventDto(
                videoId = UUID.fromString(videoId),
                userId = UUID.randomUUID(),
                userEmail = "event1@example.com",
                userName = "Event User 1"
            ), "upload"),
            Pair(mapOf("videoId" to videoId, "status" to "processing"), "processing"),
            Pair(mapOf("videoId" to videoId, "result" to "completed"), "completion")
        )

        val future = CompletableFuture<SendResult<String, Any>>()
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any())).thenReturn(future)

        events.forEach { (event, eventType) ->
            videoEventGateway.publishToProcessingTopic(event, eventType, videoId)
        }

        verify(kafkaTemplate, times(3)).send(
            eq(KafkaTopicConfig.VIDEO_PROCESSING_TOPIC),
            eq("video-$videoId"),
            any()
        )
    }
}
