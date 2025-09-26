package hackaton.fiapx.adapters.gateways

import hackaton.fiapx.commons.config.KafkaTopicConfig
import hackaton.fiapx.commons.interfaces.gateways.VideoEventGateway
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class VideoEventGatewayImpl(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : VideoEventGateway {

    private val logger = LoggerFactory.getLogger(VideoEventGatewayImpl::class.java)

    override fun sendToDlq(originalEvent: Any, exception: Throwable) {
        try {
            val dlqEvent = mapOf(
                "originalEvent" to originalEvent,
                "eventType" to originalEvent::class.simpleName,
                "failureReason" to exception.message,
                "failureTimestamp" to System.currentTimeMillis(),
                "stackTrace" to exception.stackTraceToString()
            )

            kafkaTemplate.send(KafkaTopicConfig.VIDEO_PROCESSING_DLQ, "failure", dlqEvent)
            logger.info("Sent failed event to DLQ: ${originalEvent::class.simpleName}")
        } catch (dlqException: Exception) {
            logger.error("Critical: Failed to send event to DLQ", dlqException)
        }
    }

    override fun publishToProcessingTopic(event: Any, eventType: String, videoId: String) {
        val key = "video-${videoId}"

        logger.info("Publishing $eventType event for videoId: $videoId")

        val future: CompletableFuture<SendResult<String, Any>> = kafkaTemplate.send(
            KafkaTopicConfig.VIDEO_PROCESSING_TOPIC,
            key,
            event
        )

        future.whenComplete { result, exception ->
            if (exception != null) {
                logger.error("Failed to publish $eventType event for videoId: $videoId", exception)
                handlePublishingFailure(event, eventType, exception)
            } else {
                logger.info(
                    "Successfully published $eventType event for videoId: $videoId " +
                    "to partition: ${result.recordMetadata.partition()}, " +
                    "offset: ${result.recordMetadata.offset()}"
                )
            }
        }
    }

    private fun handlePublishingFailure(event: Any, eventType: String, exception: Throwable) {
        try {
            val dlqEvent = mapOf(
                "originalEvent" to event,
                "eventType" to eventType,
                "failureReason" to exception.message,
                "failureTimestamp" to System.currentTimeMillis()
            )

            kafkaTemplate.send(KafkaTopicConfig.VIDEO_PROCESSING_DLQ, "failure", dlqEvent)
            logger.info("Sent failed $eventType event to DLQ")
        } catch (dlqException: Exception) {
            logger.error("Critical: Failed to send event to DLQ", dlqException)
        }
    }

}