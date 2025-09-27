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

        verify(valueOperations).set(
            eq("video:status:$videoId"),
            eq(status),
            eq(Duration.ofMinutes(30))
        )
    }

    @Test
    fun `caches video processing status with custom ttl`() {
        val videoId = UUID.randomUUID()
        val status = "SUCCESS"
        val customTtl = 60L

        redisCacheUseCase.cacheVideoProcessingStatus(videoId, status, customTtl)

        verify(valueOperations).set(
            eq("video:status:$videoId"),
            eq(status),
            eq(Duration.ofMinutes(customTtl))
        )
    }

    @Test
    fun `handles redis exception during status caching gracefully`() {
        val videoId = UUID.randomUUID()
        val status = "ERROR"
        `when`(valueOperations.set(any(), any(), any<Duration>())).thenThrow(RuntimeException("Redis connection failed"))

        redisCacheUseCase.cacheVideoProcessingStatus(videoId, status)

        verify(valueOperations).set(any(), any(), any<Duration>())
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

        verify(valueOperations).set(eq("video:status:$videoId1"), eq(status1), any<Duration>())
        verify(valueOperations).set(eq("video:status:$videoId2"), eq(status2), any<Duration>())
    }
}
