package hackaton.fiapx.commons.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConsumerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    /**
     * Default consumer factory for most use cases
     */
    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        val configProps = mutableMapOf<String, Any>()

        // Basic Configuration
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        configProps[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JsonDeserializer::class.java

        // Consumer behavior
        configProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        configProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false // Manual acknowledgment
        configProps[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 10 // Process in small batches

        // Session and heartbeat configuration
        configProps[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 30000 // 30 seconds
        configProps[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 3000 // 3 seconds
        configProps[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 300000 // 5 minutes

        // Fetch configuration for efficiency
        configProps[ConsumerConfig.FETCH_MIN_BYTES_CONFIG] = 1024 // 1KB
        configProps[ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG] = 500 // 500ms

        // JSON deserializer configuration
        configProps[JsonDeserializer.TRUSTED_PACKAGES] = "hackaton.fiapx.commons.dto.kafka"
        configProps[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        configProps[JsonDeserializer.VALUE_DEFAULT_TYPE] = "java.lang.Object"

        return DefaultKafkaConsumerFactory(configProps)
    }

    /**
     * Default listener container factory
     */
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory()

        // Container properties
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.pollTimeout = 3000

        // Concurrency
        factory.setConcurrency(2)

        return factory
    }

    /**
     * High-throughput consumer factory for bulk processing
     */
    @Bean("highThroughputConsumerFactory")
    fun highThroughputConsumerFactory(): ConsumerFactory<String, Any> {
        val configProps = mutableMapOf<String, Any>()

        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = "$groupId-bulk"
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        configProps[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JsonDeserializer::class.java

        // High-throughput optimizations
        configProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        configProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        configProps[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 100 // Larger batches

        // Reduced timeouts for faster processing
        configProps[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 15000 // 15 seconds
        configProps[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 2000 // 2 seconds
        configProps[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 600000 // 10 minutes

        // Optimized fetch configuration
        configProps[ConsumerConfig.FETCH_MIN_BYTES_CONFIG] = 50000 // 50KB
        configProps[ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG] = 100 // 100ms

        configProps[JsonDeserializer.TRUSTED_PACKAGES] = "hackaton.fiapx.commons.dto.kafka"
        configProps[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        configProps[JsonDeserializer.VALUE_DEFAULT_TYPE] = "java.lang.Object"

        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean("highThroughputKafkaListenerContainerFactory")
    fun highThroughputKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = highThroughputConsumerFactory()

        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.pollTimeout = 1000

        factory.setConcurrency(4) // Higher concurrency

        return factory
    }

    /**
     * Low-latency consumer factory for critical events
     */
    @Bean("lowLatencyConsumerFactory")
    fun lowLatencyConsumerFactory(): ConsumerFactory<String, Any> {
        val configProps = mutableMapOf<String, Any>()

        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = "$groupId-critical"
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        configProps[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JsonDeserializer::class.java

        // Low-latency optimizations
        configProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        configProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        configProps[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 1 // Process immediately

        // Very short timeouts
        configProps[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 10000 // 10 seconds
        configProps[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 1000 // 1 second
        configProps[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 60000 // 1 minute

        // Immediate fetch
        configProps[ConsumerConfig.FETCH_MIN_BYTES_CONFIG] = 1
        configProps[ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG] = 10 // 10ms

        configProps[JsonDeserializer.TRUSTED_PACKAGES] = "hackaton.fiapx.commons.dto.kafka"
        configProps[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        configProps[JsonDeserializer.VALUE_DEFAULT_TYPE] = "java.lang.Object"

        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean("lowLatencyKafkaListenerContainerFactory")
    fun lowLatencyKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = lowLatencyConsumerFactory()

        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.pollTimeout = 100 // Very short polling

        factory.setConcurrency(1) // Single-threaded for guaranteed order

        return factory
    }

}