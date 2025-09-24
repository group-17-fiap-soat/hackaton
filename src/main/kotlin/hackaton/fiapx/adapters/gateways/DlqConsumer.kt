package hackaton.fiapx.adapters.gateways

import hackaton.fiapx.commons.config.KafkaTopicConfig
import hackaton.fiapx.commons.interfaces.services.DlqService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class DlqConsumer(
    private val dlqService: DlqService
) {
    private val logger = LoggerFactory.getLogger(DlqConsumer::class.java)

    @KafkaListener(
        topics = [KafkaTopicConfig.VIDEO_PROCESSING_DLQ],
        groupId = "dlq-processor-group"
    )
    fun handleDlqMessage(
        @Payload dlqEvent: Map<String, Any>,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.CORRELATION_ID, required = false) correlationId: String?,
        acknowledgment: Acknowledgment
    ) {
        logger.warn("Processing DLQ message from topic: $topic, correlationId: $correlationId")

        try {
            dlqService.processDlqMessage(topic, dlqEvent, correlationId)
            acknowledgment.acknowledge()
        } catch (exception: Exception) {
            logger.error("Failed to process DLQ message from topic: $topic", exception)
            acknowledgment.acknowledge()
        }
    }
}