package hackaton.fiapx.usecases

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class RedisCacheUseCase(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(RedisCacheUseCase::class.java)
    fun cacheVideoProcessingStatus(videoId: UUID, status: String, ttlMinutes: Long = 30) {
        try {
            redisTemplate.opsForValue().set(
                "video:status:$videoId",
                status,
                Duration.ofMinutes(ttlMinutes)
            )
            logger.debug("Cached video $videoId status: $status")
        } catch (e: Exception) {
            logger.warn("Failed to cache video status for $videoId: ${e.message}")
        }
    }

    fun acquireProcessingLock(videoId: UUID, ttlMinutes: Long = 10): Boolean {
        return try {
            val lockKey = "lock:video:$videoId"
            val acquired = redisTemplate.opsForValue().setIfAbsent(
                lockKey,
                "processing",
                Duration.ofMinutes(ttlMinutes)
            ) ?: false

            logger.debug("Lock acquisition for video $videoId: $acquired")
            acquired
        } catch (e: Exception) {
            logger.warn("Failed to acquire lock for video $videoId: ${e.message}. Allowing processing to continue.")
            // Se Redis falhar, permitir que o processamento continue
            true
        }
    }

    fun releaseProcessingLock(videoId: UUID): Boolean {
        return try {
            val released = redisTemplate.delete("lock:video:$videoId")
            logger.debug("Released lock for video $videoId: $released")
            released
        } catch (e: Exception) {
            logger.warn("Failed to release lock for video $videoId: ${e.message}")
            false
        }
    }
}
