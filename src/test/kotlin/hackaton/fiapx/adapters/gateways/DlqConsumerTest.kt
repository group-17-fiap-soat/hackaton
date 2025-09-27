package hackaton.fiapx.adapters.gateways

import hackaton.fiapx.commons.config.KafkaTopicConfig
import hackaton.fiapx.commons.interfaces.services.DlqService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.kafka.support.Acknowledgment
import kotlin.test.assertEquals

class DlqConsumerTest {

    private lateinit var dlqService: DlqService
    private lateinit var dlqConsumer: DlqConsumer
    private lateinit var acknowledgment: Acknowledgment

    @BeforeEach
    fun setup() {
        dlqService = mock()
        acknowledgment = mock()
        dlqConsumer = DlqConsumer(dlqService)
    }

    @Test
    fun `handles DLQ message successfully and acknowledges`() {
        val dlqEvent = mapOf(
            "originalEvent" to mapOf("videoId" to "123"),
            "eventType" to "VideoEventDto",
            "failureReason" to "Processing failed",
            "failureTimestamp" to System.currentTimeMillis()
        )
        val topic = KafkaTopicConfig.VIDEO_PROCESSING_DLQ
        val correlationId = "correlation-123"

        dlqConsumer.handleDlqMessage(dlqEvent, topic, correlationId, acknowledgment)

        verify(dlqService).processDlqMessage(topic, dlqEvent, correlationId)
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `acknowledges message when DLQ service processing fails`() {
        val dlqEvent = mapOf(
            "originalEvent" to mapOf("videoId" to "456"),
            "eventType" to "VideoEventDto",
            "failureReason" to "Original failure",
            "failureTimestamp" to System.currentTimeMillis()
        )
        val topic = KafkaTopicConfig.VIDEO_PROCESSING_DLQ
        val correlationId = "correlation-456"

        whenever(dlqService.processDlqMessage(any(), any(), any())).thenThrow(RuntimeException("DLQ processing failed"))

        dlqConsumer.handleDlqMessage(dlqEvent, topic, correlationId, acknowledgment)

        verify(dlqService).processDlqMessage(topic, dlqEvent, correlationId)
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `handles message with null correlation ID`() {
        val dlqEvent = mapOf(
            "originalEvent" to mapOf("videoId" to "789"),
            "eventType" to "VideoEventDto",
            "failureReason" to "Null correlation test"
        )
        val topic = KafkaTopicConfig.VIDEO_PROCESSING_DLQ

        dlqConsumer.handleDlqMessage(dlqEvent, topic, null, acknowledgment)

        verify(dlqService).processDlqMessage(topic, dlqEvent, null)
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `handles empty DLQ event map`() {
        val dlqEvent = emptyMap<String, Any>()
        val topic = KafkaTopicConfig.VIDEO_PROCESSING_DLQ
        val correlationId = "empty-event"

        dlqConsumer.handleDlqMessage(dlqEvent, topic, correlationId, acknowledgment)

        verify(dlqService).processDlqMessage(topic, dlqEvent, correlationId)
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `handles DLQ event with nested complex data structures`() {
        val complexEvent = mapOf(
            "originalEvent" to mapOf(
                "videoId" to "complex-123",
                "userData" to mapOf(
                    "id" to "user-456",
                    "email" to "user@example.com",
                    "preferences" to listOf("hd", "notifications")
                )
            ),
            "eventType" to "ComplexVideoEventDto",
            "failureReason" to "Complex processing failed",
            "stackTrace" to "java.lang.RuntimeException: Complex error\n\tat ...",
            "metadata" to mapOf(
                "retryCount" to 3,
                "lastAttempt" to System.currentTimeMillis()
            )
        )
        val topic = KafkaTopicConfig.VIDEO_PROCESSING_DLQ
        val correlationId = "complex-correlation"

        dlqConsumer.handleDlqMessage(complexEvent, topic, correlationId, acknowledgment)

        verify(dlqService).processDlqMessage(topic, complexEvent, correlationId)
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `handles DLQ event with all expected failure fields`() {
        val timestamp = System.currentTimeMillis()
        val dlqEvent = mapOf(
            "originalEvent" to mapOf(
                "videoId" to "full-event-123",
                "userId" to "user-789"
            ),
            "eventType" to "VideoUploadEvent",
            "failureReason" to "Database connection timeout",
            "failureTimestamp" to timestamp,
            "stackTrace" to "java.sql.SQLException: Connection timeout\n\tat database.connect()",
            "retryAttempts" to 5
        )
        val topic = KafkaTopicConfig.VIDEO_PROCESSING_DLQ
        val correlationId = "full-event-correlation"

        dlqConsumer.handleDlqMessage(dlqEvent, topic, correlationId, acknowledgment)

        verify(dlqService).processDlqMessage(topic, dlqEvent, correlationId)
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `passes correct parameters to DLQ service`() {
        val dlqEvent = mapOf(
            "originalEvent" to mapOf("test" to "data"),
            "eventType" to "TestEvent"
        )
        val topic = "test-topic"
        val correlationId = "test-correlation"

        dlqConsumer.handleDlqMessage(dlqEvent, topic, correlationId, acknowledgment)

        val topicCaptor = argumentCaptor<String>()
        val eventCaptor = argumentCaptor<Map<String, Any>>()
        val correlationCaptor = argumentCaptor<String>()

        verify(dlqService).processDlqMessage(
            topicCaptor.capture(),
            eventCaptor.capture(),
            correlationCaptor.capture()
        )

        assertEquals(topic, topicCaptor.firstValue)
        assertEquals(dlqEvent, eventCaptor.firstValue)
        assertEquals(correlationId, correlationCaptor.firstValue)
    }

    @Test
    fun `handles DLQ event with missing expected fields gracefully`() {
        val incompleteEvent = mapOf(
            "eventType" to "IncompleteEvent"
        )
        val topic = KafkaTopicConfig.VIDEO_PROCESSING_DLQ
        val correlationId = "incomplete-correlation"

        dlqConsumer.handleDlqMessage(incompleteEvent, topic, correlationId, acknowledgment)

        verify(dlqService).processDlqMessage(topic, incompleteEvent, correlationId)
        verify(acknowledgment).acknowledge()
    }

    @Test
    fun `handles concurrent DLQ message processing`() {
        val events = (1..5).map { i ->
            mapOf(
                "originalEvent" to mapOf("videoId" to "concurrent-$i"),
                "eventType" to "ConcurrentEvent$i",
                "failureReason" to "Concurrent processing test $i"
            )
        }
        val topic = KafkaTopicConfig.VIDEO_PROCESSING_DLQ

        events.forEachIndexed { index, event ->
            val correlationId = "concurrent-$index"
            dlqConsumer.handleDlqMessage(event, topic, correlationId, acknowledgment)
        }

        verify(dlqService, times(5)).processDlqMessage(any(), any(), any())
        verify(acknowledgment, times(5)).acknowledge()
    }
}
