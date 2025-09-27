package hackaton.fiapx.commons.dto.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VideoEventDtoTest {

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = jacksonObjectMapper()
    }

    @Test
    fun `creates video event with all properties`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val userEmail = "test@example.com"
        val userName = "Test User"

        val videoEvent = VideoEventDto(
            videoId = videoId,
            userId = userId,
            userEmail = userEmail,
            userName = userName
        )

        assertEquals(videoId, videoEvent.videoId)
        assertEquals(userId, videoEvent.userId)
        assertEquals(userEmail, videoEvent.userEmail)
        assertEquals(userName, videoEvent.userName)
    }

    @Test
    fun `creates video event with null user name`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val userEmail = "nullname@example.com"

        val videoEvent = VideoEventDto(
            videoId = videoId,
            userId = userId,
            userEmail = userEmail,
            userName = null
        )

        assertEquals(videoId, videoEvent.videoId)
        assertEquals(userId, videoEvent.userId)
        assertEquals(userEmail, videoEvent.userEmail)
        assertEquals(null, videoEvent.userName)
    }

    @Test
    fun `serializes to JSON correctly`() {
        val videoId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val userId = UUID.fromString("987fcdeb-51a2-43d9-b987-123456789abc")
        val videoEvent = VideoEventDto(
            videoId = videoId,
            userId = userId,
            userEmail = "serialize@example.com",
            userName = "Serialize User"
        )

        val json = objectMapper.writeValueAsString(videoEvent)

        assertNotNull(json)
        assert(json.contains("123e4567-e89b-12d3-a456-426614174000"))
        assert(json.contains("987fcdeb-51a2-43d9-b987-123456789abc"))
        assert(json.contains("serialize@example.com"))
        assert(json.contains("Serialize User"))
    }

    @Test
    fun `deserializes from JSON correctly`() {
        val json = """
            {
                "eventType": "video_upload",
                "videoId": "123e4567-e89b-12d3-a456-426614174000",
                "userId": "987fcdeb-51a2-43d9-b987-123456789abc",
                "userEmail": "deserialize@example.com",
                "userName": "Deserialize User"
            }
        """.trimIndent()

        val videoEvent = objectMapper.readValue(json, VideoEventDto::class.java)

        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), videoEvent.videoId)
        assertEquals(UUID.fromString("987fcdeb-51a2-43d9-b987-123456789abc"), videoEvent.userId)
        assertEquals("deserialize@example.com", videoEvent.userEmail)
        assertEquals("Deserialize User", videoEvent.userName)
    }

    @Test
    fun `deserializes from JSON with null userName`() {
        val json = """
            {
                "eventType": "video_upload",
                "videoId": "123e4567-e89b-12d3-a456-426614174000",
                "userId": "987fcdeb-51a2-43d9-b987-123456789abc",
                "userEmail": "nulluser@example.com",
                "userName": null
            }
        """.trimIndent()

        val videoEvent = objectMapper.readValue(json, VideoEventDto::class.java)

        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), videoEvent.videoId)
        assertEquals(UUID.fromString("987fcdeb-51a2-43d9-b987-123456789abc"), videoEvent.userId)
        assertEquals("nulluser@example.com", videoEvent.userEmail)
        assertEquals(null, videoEvent.userName)
    }

    @Test
    fun `handles round trip serialization and deserialization`() {
        val originalEvent = VideoEventDto(
            videoId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            userEmail = "roundtrip@example.com",
            userName = "Round Trip User"
        )

        val json = objectMapper.writeValueAsString(originalEvent)
        val deserializedEvent = objectMapper.readValue(json, VideoEventDto::class.java)

        assertEquals(originalEvent.videoId, deserializedEvent.videoId)
        assertEquals(originalEvent.userId, deserializedEvent.userId)
        assertEquals(originalEvent.userEmail, deserializedEvent.userEmail)
        assertEquals(originalEvent.userName, deserializedEvent.userName)
    }

    @Test
    fun `maintains data class equality and hashCode`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val userEmail = "equality@example.com"
        val userName = "Equality User"

        val event1 = VideoEventDto(videoId, userId, userEmail, userName)
        val event2 = VideoEventDto(videoId, userId, userEmail, userName)
        val event3 = VideoEventDto(UUID.randomUUID(), userId, userEmail, userName)

        assertEquals(event1, event2)
        assertEquals(event1.hashCode(), event2.hashCode())
        assert(event1 != event3)
        assert(event1.hashCode() != event3.hashCode())
    }

    @Test
    fun `provides meaningful toString representation`() {
        val videoEvent = VideoEventDto(
            videoId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            userId = UUID.fromString("987fcdeb-51a2-43d9-b987-123456789abc"),
            userEmail = "tostring@example.com",
            userName = "ToString User"
        )

        val toString = videoEvent.toString()

        assert(toString.contains("VideoEventDto"))
        assert(toString.contains("123e4567-e89b-12d3-a456-426614174000"))
        assert(toString.contains("tostring@example.com"))
    }

    @Test
    fun `handles different email formats`() {
        val emailFormats = listOf(
            "user@domain.com",
            "user.name@domain.co.uk",
            "user+tag@domain.org",
            "user123@sub.domain.com"
        )

        emailFormats.forEach { email ->
            val videoEvent = VideoEventDto(
                videoId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                userEmail = email,
                userName = "Test User"
            )

            assertEquals(email, videoEvent.userEmail)
        }
    }

    @Test
    fun `component functions work correctly`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val userEmail = "component@example.com"
        val userName = "Component User"

        val videoEvent = VideoEventDto(videoId, userId, userEmail, userName)

        val (extractedVideoId, extractedUserId, extractedEmail, extractedName) = videoEvent

        assertEquals(videoId, extractedVideoId)
        assertEquals(userId, extractedUserId)
        assertEquals(userEmail, extractedEmail)
        assertEquals(userName, extractedName)
    }
}
