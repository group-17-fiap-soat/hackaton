package hackaton.fiapx.adapters.services

import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.commons.interfaces.services.DlqService
import hackaton.fiapx.usecases.process.ProcessVideoUseCase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class DlqServiceImpl(
    private val processVideoUseCase: ProcessVideoUseCase,
    private val videoGateway: VideoGatewayInterface
) : DlqService {
    private val logger = LoggerFactory.getLogger(DlqServiceImpl::class.java)

    override fun processDlqMessage(topic: String, dlqEvent: Map<String, Any>, correlationId: String?) {
        logger.info("Processing DLQ message from topic: $topic, correlationId: $correlationId")

        val originalEvent = dlqEvent["originalEvent"]
        val dlqTimestamp = dlqEvent["dlqTimestamp"] as? Long
        val reason = dlqEvent["reason"] as? String
        val originalTopic = dlqEvent["originalTopic"] as? String

        logger.warn(
            "DLQ Event Details - Topic: $topic, Reason: $reason, " +
                    "Original Topic: $originalTopic, Timestamp: $dlqTimestamp, CorrelationId: $correlationId"
        )

        logDlqEvent(topic, dlqEvent, correlationId)

        sendDlqAlert(topic, dlqEvent, correlationId)

        val eventType = dlqEvent["eventType"] as? String
        logger.info("Processing DLQ event of type: $eventType, correlationId: $correlationId")

        when (eventType) {
            "VideoUploadEvent" -> handleVideoUploadDlqEvent(originalEvent, correlationId)
            else -> logger.warn("Unknown event type in DLQ: $eventType")
        }
    }

    private fun handleVideoUploadDlqEvent(originalEvent: Any?, correlationId: String?) {
        logger.info("Handling video upload DLQ event, correlationId: $correlationId")

        try {
            if (originalEvent is Map<*, *>) {
                val videoId = originalEvent["videoId"] as UUID
                val video = videoGateway.findById(videoId)

                if (video != null) {
                    logger.info("Attempting to retry video processing for videoId: $videoId")

                    processVideoUseCase.execute(video)

                    updateVideoStatus(
                        videoId,
                        VideoProcessStatusEnum.FINISHED,
                        "Video processing completed after DLQ retry"
                    )
                    logger.info("Successfully processed video $videoId from DLQ")

                } else {
                    logger.error("Invalid video upload event in DLQ - missing videoId or userId")
                }
            } else {
                logger.error("Invalid DLQ event format for video upload")
            }

        } catch (exception: Exception) {
            logger.error("Failed to retry video processing from DLQ", exception)

            if (originalEvent is Map<*, *>) {
                val videoId = originalEvent["videoId"] as UUID
                updateVideoStatus(
                    videoId,
                    VideoProcessStatusEnum.ERROR,
                    "Video processing failed after DLQ retry: ${exception.message}"
                )
                logger.error("Marked video $videoId as ERROR after DLQ retry failure")
            }
        }
    }

    private fun updateVideoStatus(videoId: UUID, status: VideoProcessStatusEnum, message: String? = null) {
        try {
            val currentVideo = videoGateway.findById(videoId)
            if (currentVideo != null) {
                val updatedVideo = currentVideo.copy(
                    status = status,
                    errorMessage = message
                )
                videoGateway.save(updatedVideo)
                logger.info("Updated video $videoId status to $status")
            } else {
                logger.warn("Video $videoId not found for status update")
            }
        } catch (exception: Exception) {
            logger.error("Failed to update video $videoId status to $status", exception)
        }
    }

    private fun logDlqEvent(topic: String, dlqEvent: Map<String, Any>, correlationId: String?) {
        // TODO: Implement database logging
        logger.info("Logging DLQ event to database for topic: $topic, correlationId: $correlationId")
    }

    private fun sendDlqAlert(topic: String, dlqEvent: Map<String, Any>, correlationId: String?) {
        // TODO: Implement monitoring system alert
        logger.warn("Sending DLQ alert for topic: $topic, correlationId: $correlationId")
    }
}