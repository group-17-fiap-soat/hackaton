package hackaton.fiapx.commons.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaProducerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val configProps = mutableMapOf<String, Any>()

        // Basic Configuration
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java

        // Performance Optimization
        configProps[ProducerConfig.ACKS_CONFIG] = "all" // Wait for all replicas
        configProps[ProducerConfig.RETRIES_CONFIG] = 3
        configProps[ProducerConfig.BATCH_SIZE_CONFIG] = 32768 // 32KB batches
        configProps[ProducerConfig.LINGER_MS_CONFIG] = 10 // Wait 10ms for batching
        configProps[ProducerConfig.BUFFER_MEMORY_CONFIG] = 67108864 // 64MB buffer

        // Reliability Configuration
        configProps[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true
        configProps[ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION] = 5
        configProps[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = 120000 // 2 minutes

        // Compression for efficiency
        configProps[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "snappy"

        // Timeout configurations
        configProps[ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG] = 30000 // 30 seconds
        configProps[ProducerConfig.METADATA_MAX_AGE_CONFIG] = 300000 // 5 minutes

        // Custom configurations for JSON serialization
        configProps[JsonSerializer.ADD_TYPE_INFO_HEADERS] = false

        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> {
        val template = KafkaTemplate(producerFactory())

        // Set default topic (optional)
        // template.defaultTopic = "default-topic"

        // Enable observation for metrics
        template.setObservationEnabled(true)

        return template
    }

    /**
     * High-throughput producer factory for bulk operations
     */
    @Bean("highThroughputProducerFactory")
    fun highThroughputProducerFactory(): ProducerFactory<String, Any> {
        val configProps = mutableMapOf<String, Any>()

        // Basic Configuration
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java

        // High-throughput optimizations
        configProps[ProducerConfig.ACKS_CONFIG] = "1" // Wait only for leader
        configProps[ProducerConfig.RETRIES_CONFIG] = 1
        configProps[ProducerConfig.BATCH_SIZE_CONFIG] = 65536 // 64KB batches
        configProps[ProducerConfig.LINGER_MS_CONFIG] = 50 // Wait longer for larger batches
        configProps[ProducerConfig.BUFFER_MEMORY_CONFIG] = 134217728 // 128MB buffer

        // Compression for network efficiency
        configProps[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "lz4"

        // Disable idempotence for higher throughput
        configProps[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = false
        configProps[ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION] = 10

        // Shorter timeouts for faster failure detection
        configProps[ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG] = 15000 // 15 seconds
        configProps[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = 60000 // 1 minute

        configProps[JsonSerializer.ADD_TYPE_INFO_HEADERS] = false

        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean("highThroughputKafkaTemplate")
    fun highThroughputKafkaTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(highThroughputProducerFactory())
    }

    /**
     * Critical events producer factory with maximum reliability
     */
    @Bean("criticalEventsProducerFactory")
    fun criticalEventsProducerFactory(): ProducerFactory<String, Any> {
        val configProps = mutableMapOf<String, Any>()

        // Basic Configuration
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java

        // Maximum reliability configurations
        configProps[ProducerConfig.ACKS_CONFIG] = "all" // Wait for all replicas
        configProps[ProducerConfig.RETRIES_CONFIG] = Integer.MAX_VALUE
        configProps[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true
        configProps[ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION] = 1 // Ensure ordering

        // Conservative batching for immediate delivery
        configProps[ProducerConfig.BATCH_SIZE_CONFIG] = 1 // Send immediately
        configProps[ProducerConfig.LINGER_MS_CONFIG] = 0 // No waiting

        // Extended timeouts for reliability
        configProps[ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG] = 60000 // 1 minute
        configProps[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = 300000 // 5 minutes

        // No compression for faster processing
        configProps[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "none"

        configProps[JsonSerializer.ADD_TYPE_INFO_HEADERS] = false

        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean("criticalEventsKafkaTemplate")
    fun criticalEventsKafkaTemplate(): KafkaTemplate<String, Any> {
        val template = KafkaTemplate(criticalEventsProducerFactory())
        template.setObservationEnabled(true)
        return template
    }
}