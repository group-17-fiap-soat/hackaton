package hackaton.fiapx.usecases

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RedisCacheUseCaseTest {

    private lateinit var redisTemplate: RedisTemplate<String, Any>
    private lateinit var valueOperations: ValueOperations<String, Any>
    private lateinit var redisCacheUseCase: RedisCacheUseCase

    @BeforeEach
    fun setup() {
        redisTemplate = mock(RedisTemplate::class.java) as RedisTemplate<String, Any>
        valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, Any>
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
        redisCacheUseCase = RedisCacheUseCase(redisTemplate)
    }

    @Test
    fun `caches video processing status successfully`() {
        val videoId = UUID.randomUUID()
        val status = "PROCESSING"

        redisCacheUseCase.cacheVideoProcessingStatus(videoId, status)

        verify(valueOperations).set("video:status:$videoId", status)
        verify(redisTemplate).expire("video:status:$videoId", Duration.ofMinutes(30))
    }

    @Test
    fun `caches video processing status with custom ttl`() {
        val videoId = UUID.randomUUID()
        val status = "SUCCESS"
        val customTtl = 60L

        redisCacheUseCase.cacheVideoProcessingStatus(videoId, status, customTtl)

        verify(valueOperations).set("video:status:$videoId", status)
        verify(redisTemplate).expire("video:status:$videoId", Duration.ofMinutes(customTtl))
    }

    @Test
    fun `handles redis exception during status caching gracefully`() {
        val videoId = UUID.randomUUID()
        val status = "ERROR"
        `when`(valueOperations.set(any(), any())).thenThrow(RuntimeException("Redis connection failed"))

        redisCacheUseCase.cacheVideoProcessingStatus(videoId, status)

        verify(valueOperations).set(any(), any())
    }

    @Test
    fun `acquires processing lock successfully when not exists`() {
        val videoId = UUID.randomUUID()
        `when`(valueOperations.setIfAbsent(any(), any(), any<Duration>())).thenReturn(true)

        val result = redisCacheUseCase.acquireProcessingLock(videoId)

        assertTrue(result)
        verify(valueOperations).setIfAbsent(
            eq("lock:video:$videoId"),
            eq("processing"),
            eq(Duration.ofMinutes(10))
        )
    }

    @Test
    fun `fails to acquire processing lock when already exists`() {
        val videoId = UUID.randomUUID()
        `when`(valueOperations.setIfAbsent(any(), any(), any<Duration>())).thenReturn(false)

        val result = redisCacheUseCase.acquireProcessingLock(videoId)

        assertFalse(result)
    }

    @Test
    fun `acquires processing lock with custom ttl`() {
        val videoId = UUID.randomUUID()
        val customTtl = 5L
        `when`(valueOperations.setIfAbsent(any(), any(), any<Duration>())).thenReturn(true)

        val result = redisCacheUseCase.acquireProcessingLock(videoId, customTtl)

        assertTrue(result)
        verify(valueOperations).setIfAbsent(
            eq("lock:video:$videoId"),
            eq("processing"),
            eq(Duration.ofMinutes(customTtl))
        )
    }

    @Test
    fun `allows processing to continue when redis fails during lock acquisition`() {
        val videoId = UUID.randomUUID()
        `when`(valueOperations.setIfAbsent(any(), any(), any<Duration>())).thenThrow(RuntimeException("Redis connection failed"))

        val result = redisCacheUseCase.acquireProcessingLock(videoId)

        assertTrue(result)
    }

    @Test
    fun `handles null return from redis during lock acquisition`() {
        val videoId = UUID.randomUUID()
        `when`(valueOperations.setIfAbsent(any(), any(), any<Duration>())).thenReturn(null)

        val result = redisCacheUseCase.acquireProcessingLock(videoId)

        assertFalse(result)
    }

    @Test
    fun `releases processing lock successfully`() {
        val videoId = UUID.randomUUID()
        `when`(redisTemplate.delete(any<String>())).thenReturn(true)

        val result = redisCacheUseCase.releaseProcessingLock(videoId)

        assertTrue(result)
        verify(redisTemplate).delete(eq("lock:video:$videoId"))
    }

    @Test
    fun `returns false when lock does not exist during release`() {
        val videoId = UUID.randomUUID()
        `when`(redisTemplate.delete(any<String>())).thenReturn(false)

        val result = redisCacheUseCase.releaseProcessingLock(videoId)

        assertFalse(result)
    }

    @Test
    fun `handles redis exception during lock release gracefully`() {
        val videoId = UUID.randomUUID()
        `when`(redisTemplate.delete(any<String>())).thenThrow(RuntimeException("Redis connection failed"))

        val result = redisCacheUseCase.releaseProcessingLock(videoId)

        assertFalse(result)
        verify(redisTemplate).delete(any<String>())
    }

    @Test
    fun `processes different video ids independently for lock operations`() {
        val videoId1 = UUID.randomUUID()
        val videoId2 = UUID.randomUUID()
        `when`(valueOperations.setIfAbsent(any(), any(), any<Duration>())).thenReturn(true)

        redisCacheUseCase.acquireProcessingLock(videoId1)
        redisCacheUseCase.acquireProcessingLock(videoId2)

        verify(valueOperations).setIfAbsent(eq("lock:video:$videoId1"), any(), any<Duration>())
        verify(valueOperations).setIfAbsent(eq("lock:video:$videoId2"), any(), any<Duration>())
    }

    @Test
    fun `handles concurrent lock acquisition attempts for same video`() {
        val videoId = UUID.randomUUID()
        `when`(valueOperations.setIfAbsent(any(), any(), any<Duration>()))
            .thenReturn(true)
            .thenReturn(false)

        val firstAttempt = redisCacheUseCase.acquireProcessingLock(videoId)
        val secondAttempt = redisCacheUseCase.acquireProcessingLock(videoId)

        assertTrue(firstAttempt)
        assertFalse(secondAttempt)
    }

    @Test
    fun `caches status for multiple videos independently`() {
        val videoId1 = UUID.randomUUID()
        val videoId2 = UUID.randomUUID()
        val status1 = "PROCESSING"
        val status2 = "SUCCESS"

        redisCacheUseCase.cacheVideoProcessingStatus(videoId1, status1)
        redisCacheUseCase.cacheVideoProcessingStatus(videoId2, status2)

        verify(valueOperations).set("video:status:$videoId1", status1)
        verify(valueOperations).set("video:status:$videoId2", status2)
        verify(redisTemplate).expire("video:status:$videoId1", Duration.ofMinutes(30))
        verify(redisTemplate).expire("video:status:$videoId2", Duration.ofMinutes(30))
    }

    @Test
    fun `retrieves video processing status successfully`() {
        val videoId = UUID.randomUUID()
        val expectedStatus = "PROCESSING"
        `when`(valueOperations.get("video:status:$videoId")).thenReturn(expectedStatus)

        val result = redisCacheUseCase.getVideoProcessingStatus(videoId)

        assertEquals(expectedStatus, result)
        verify(valueOperations).get("video:status:$videoId")
    }

    @Test
    fun `returns null when video status does not exist`() {
        val videoId = UUID.randomUUID()
        `when`(valueOperations.get("video:status:$videoId")).thenReturn(null)

        val result = redisCacheUseCase.getVideoProcessingStatus(videoId)

        assertNull(result)
    }

    @Test
    fun `handles redis exception during status retrieval gracefully`() {
        val videoId = UUID.randomUUID()
        `when`(valueOperations.get(any())).thenThrow(RuntimeException("Redis connection failed"))

        val result = redisCacheUseCase.getVideoProcessingStatus(videoId)

        assertNull(result)
    }

    @Test
    fun `checks if video is being processed successfully`() {
        val videoId = UUID.randomUUID()
        `when`(redisTemplate.hasKey("lock:video:$videoId")).thenReturn(true)

        val result = redisCacheUseCase.isVideoBeingProcessed(videoId)

        assertTrue(result)
        verify(redisTemplate).hasKey("lock:video:$videoId")
    }

    @Test
    fun `returns false when video is not being processed`() {
        val videoId = UUID.randomUUID()
        `when`(redisTemplate.hasKey("lock:video:$videoId")).thenReturn(false)

        val result = redisCacheUseCase.isVideoBeingProcessed(videoId)

        assertFalse(result)
    }

    @Test
    fun `handles redis exception during processing status check gracefully`() {
        val videoId = UUID.randomUUID()
        `when`(redisTemplate.hasKey(any<String>())).thenThrow(RuntimeException("Redis connection failed"))

        val result = redisCacheUseCase.isVideoBeingProcessed(videoId)

        assertFalse(result)
    }

    @Test
    fun `redis health check returns true when healthy`() {
        `when`(valueOperations.get("health:check")).thenReturn("ok")

        val result = redisCacheUseCase.isRedisHealthy()

        assertTrue(result)
        verify(valueOperations).set("health:check", "ok")
        verify(redisTemplate).expire("health:check", Duration.ofSeconds(10))
        verify(valueOperations).get("health:check")
    }

    @Test
    fun `redis health check returns false when unhealthy`() {
        `when`(valueOperations.get("health:check")).thenReturn("not ok")

        val result = redisCacheUseCase.isRedisHealthy()

        assertFalse(result)
    }

    @Test
    fun `redis health check handles exceptions gracefully`() {
        `when`(valueOperations.set(any(), any())).thenThrow(RuntimeException("Redis connection failed"))

        val result = redisCacheUseCase.isRedisHealthy()

        assertFalse(result)
    }
}
