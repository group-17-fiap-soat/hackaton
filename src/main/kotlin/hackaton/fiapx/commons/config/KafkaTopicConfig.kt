package hackaton.fiapx.commons.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    companion object {
        // Simplified Topic Structure - Only 2 topics needed

        // Main Video Processing Topic - handles all video events
        const val VIDEO_PROCESSING_TOPIC = "hackaton.video.processing.events"

        // Dead Letter Queue - for failed processing events
        const val VIDEO_PROCESSING_DLQ = "hackaton.video.processing.dlq"
    }

    /**
     * Main video processing topic - handles all video lifecycle events:
     * - Upload events
     * - Processing status updates
     * - Completion/Error events
     * - Progress updates
     */
    @Bean
    fun videoProcessingTopic(): NewTopic {
        return TopicBuilder.name(VIDEO_PROCESSING_TOPIC)
            .partitions(6) // High throughput for parallel processing
            .replicas(1)
            .config("retention.ms", "604800000") // 7 days retention
            .config("cleanup.policy", "delete")
            .config("segment.ms", "86400000") // 1 day segments
            .config("compression.type", "snappy") // Good balance of speed/compression
            .build()
    }

    /**
     * Dead Letter Queue - for events that fail processing after retries
     */
    @Bean
    fun videoProcessingDlqTopic(): NewTopic {
        return TopicBuilder.name(VIDEO_PROCESSING_DLQ)
            .partitions(2) // Lower throughput expected
            .replicas(1)
            .config("retention.ms", "2592000000") // 30 days retention for investigation
            .config("cleanup.policy", "delete")
            .config("compression.type", "gzip") // Better compression for long-term storage
            .build()
    }
}