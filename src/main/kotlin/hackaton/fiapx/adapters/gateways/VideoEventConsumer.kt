package hackaton.fiapx.adapters.gateways

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import hackaton.fiapx.commons.config.KafkaTopicConfig
import hackaton.fiapx.commons.dto.kafka.VideoUploadEvent
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.Video
import hackaton.fiapx.usecases.ProcessVideoUseCase
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class VideoEventConsumer(
    private val processVideoUseCase: ProcessVideoUseCase,
    private val videoGateway: VideoGatewayInterface,
) {
    private val logger = LoggerFactory.getLogger(VideoEventConsumer::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    /**
     * Main consumer - handles ALL video events from the single processing topic
     */
    @KafkaListener(
        topics = [KafkaTopicConfig.VIDEO_PROCESSING_TOPIC],
        groupId = "video-processing-group",
        concurrency = "3"
    )
    fun handleVideoEvent(
        record: ConsumerRecord<String, Any>,
        acknowledgment: Acknowledgment
    ) {
        val correlationId = record.headers().lastHeader("correlationId")?.value()?.let { String(it) }

        logger.info("Processing event from partition: ${record.partition()}, offset: ${record.offset()}, correlationId: $correlationId")
        val event = record.value()

        try {
            if (event != null) {
                val videoEvent = objectMapper.convertValue(event, VideoUploadEvent::class.java)
                handleVideoUploadEvent(videoEvent, acknowledgment)
            } else {
                logger.warn("Received null event")
                acknowledgment.acknowledge()
            }
        } catch (exception: Exception) {
            logger.error("Failed to process event of type: ${event::class.simpleName}", exception)
            acknowledgment.acknowledge()
        }
    }

    private fun handleVideoUploadEvent(
        event: VideoUploadEvent,
        acknowledgment: Acknowledgment
    ) {
        logger.info("Processing video upload event: videoId=${event.videoId}")

        videoGateway.findById(event.videoId)?.let { video ->
            try {
                updateVideoStatus(video, VideoProcessStatusEnum.PROCESSING, "Video processing started")
                processVideoUseCase.execute(video)
                updateVideoStatus(video, VideoProcessStatusEnum.FINISHED, "Video processing completed successfully")

                acknowledgment.acknowledge()
                logger.info("Successfully processed video upload event for videoId: ${event.videoId}")
            } catch (exception: Exception) {
                logger.error("Failed to process video upload event for videoId: ${event.videoId}", exception)

                updateVideoStatus(video, VideoProcessStatusEnum.ERROR, "Video processing failed: ${exception.message}")

                acknowledgment.acknowledge()
            }
        }
    }

    private fun updateVideoStatus(currentVideo: Video, status: VideoProcessStatusEnum, message: String? = null) {
        try {
            val updatedVideo = currentVideo.copy(
                status = status,
                errorMessage = message,
            )
            videoGateway.save(updatedVideo)
            logger.info("Updated video ${currentVideo.id} status to $status")
        } catch (exception: Exception) {
            logger.error("Failed to update video ${currentVideo.id} status to $status", exception)
        }
    }

}