package hackaton.fiapx.commons.dto.response

import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class VideoResponseV1Test {

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = jacksonObjectMapper()
    }

    @Test
    fun `creates video response with all properties`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val response = VideoResponseV1(
            id = id,
            userId = userId,
            originalVideoPath = "test.mp4",
            zipPath = "frames.zip",
            frameCount = 120,
            fileSize = 1048576L,
            status = VideoProcessStatusEnum.FINISHED,
            uploadedAt = uploadedAt,
            message = "Processing completed successfully"
        )

        assertEquals(id, response.id)
        assertEquals(userId, response.userId)
        assertEquals("test.mp4", response.originalVideoPath)
        assertEquals("frames.zip", response.zipPath)
        assertEquals(120, response.frameCount)
        assertEquals(1048576L, response.fileSize)
        assertEquals(VideoProcessStatusEnum.FINISHED, response.status)
        assertEquals(uploadedAt, response.uploadedAt)
        assertEquals("Processing completed successfully", response.message)
    }

    @Test
    fun `creates video response with minimal properties`() {
        val response = VideoResponseV1()

        assertNull(response.id)
        assertNull(response.userId)
        assertNull(response.originalVideoPath)
        assertNull(response.zipPath)
        assertNull(response.frameCount)
        assertNull(response.fileSize)
        assertNull(response.status)
        assertNull(response.uploadedAt)
        assertNull(response.message)
    }

    @Test
    fun `creates video response for upload success`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val response = VideoResponseV1(
            id = id,
            userId = userId,
            originalVideoPath = "uploaded.mp4",
            fileSize = 2048000L,
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = uploadedAt,
            message = "Video uploaded successfully and queued for processing"
        )

        assertEquals(VideoProcessStatusEnum.UPLOADED, response.status)
        assertNull(response.zipPath)
        assertNull(response.frameCount)
        assertNotNull(response.message)
    }

    @Test
    fun `creates video response for processing error`() {
        val id = UUID.randomUUID()

        val response = VideoResponseV1(
            id = id,
            status = VideoProcessStatusEnum.ERROR,
            message = "Video processing failed: Invalid format"
        )

        assertEquals(VideoProcessStatusEnum.ERROR, response.status)
        assertEquals("Video processing failed: Invalid format", response.message)
        assertNull(response.zipPath)
        assertNull(response.frameCount)
    }

    @Test
    fun `serializes to JSON correctly`() {
        val id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val response = VideoResponseV1(
            id = id,
            status = VideoProcessStatusEnum.FINISHED,
            frameCount = 90,
            message = "Success"
        )

        val json = objectMapper.writeValueAsString(response)

        assertNotNull(json)
        assert(json.contains("123e4567-e89b-12d3-a456-426614174000"))
        assert(json.contains("FINISHED"))
        assert(json.contains("90"))
        assert(json.contains("Success"))
    }

    @Test
    fun `deserializes from JSON correctly`() {
        val json = """
            {
                "id": "123e4567-e89b-12d3-a456-426614174000",
                "userId": "987fcdeb-51a2-43d9-b987-123456789abc",
                "originalVideoPath": "deserialize.mp4",
                "zipPath": "output.zip",
                "frameCount": 150,
                "fileSize": 3145728,
                "status": "PROCESSING",
                "message": "Currently processing video"
            }
        """.trimIndent()

        val response = objectMapper.readValue(json, VideoResponseV1::class.java)

        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), response.id)
        assertEquals(UUID.fromString("987fcdeb-51a2-43d9-b987-123456789abc"), response.userId)
        assertEquals("deserialize.mp4", response.originalVideoPath)
        assertEquals("output.zip", response.zipPath)
        assertEquals(150, response.frameCount)
        assertEquals(3145728L, response.fileSize)
        assertEquals(VideoProcessStatusEnum.PROCESSING, response.status)
        assertEquals("Currently processing video", response.message)
    }

    @Test
    fun `handles null values in JSON deserialization`() {
        val json = """
            {
                "id": null,
                "userId": null,
                "originalVideoPath": null,
                "zipPath": null,
                "frameCount": null,
                "fileSize": null,
                "status": null,
                "uploadedAt": null,
                "message": null
            }
        """.trimIndent()

        val response = objectMapper.readValue(json, VideoResponseV1::class.java)

        assertNull(response.id)
        assertNull(response.userId)
        assertNull(response.originalVideoPath)
        assertNull(response.zipPath)
        assertNull(response.frameCount)
        assertNull(response.fileSize)
        assertNull(response.status)
        assertNull(response.uploadedAt)
        assertNull(response.message)
    }

    @Test
    fun `maintains data class equality and hashCode`() {
        val id = UUID.randomUUID()
        val response1 = VideoResponseV1(id = id, status = VideoProcessStatusEnum.UPLOADED)
        val response2 = VideoResponseV1(id = id, status = VideoProcessStatusEnum.UPLOADED)
        val response3 = VideoResponseV1(id = UUID.randomUUID(), status = VideoProcessStatusEnum.UPLOADED)

        assertEquals(response1, response2)
        assertEquals(response1.hashCode(), response2.hashCode())
        assert(response1 != response3)
    }

    @Test
    fun `creates response for different video processing stages`() {
        val id = UUID.randomUUID()
        val stages = listOf(
            VideoProcessStatusEnum.UPLOADED to "Video uploaded successfully",
            VideoProcessStatusEnum.PROCESSING to "Processing video frames",
            VideoProcessStatusEnum.FINISHED to "Processing completed",
            VideoProcessStatusEnum.ERROR to "Processing failed"
        )

        stages.forEach { (status, message) ->
            val response = VideoResponseV1(
                id = id,
                status = status,
                message = message
            )

            assertEquals(status, response.status)
            assertEquals(message, response.message)
        }
    }

    @Test
    fun `handles large file sizes`() {
        val largeFileSize = 10_737_418_240L // 10GB

        val response = VideoResponseV1(
            fileSize = largeFileSize,
            status = VideoProcessStatusEnum.UPLOADED
        )

        assertEquals(largeFileSize, response.fileSize)
    }

    @Test
    fun `handles high frame counts`() {
        val highFrameCount = 7200 // 2 hours at 1fps

        val response = VideoResponseV1(
            frameCount = highFrameCount,
            status = VideoProcessStatusEnum.FINISHED
        )

        assertEquals(highFrameCount, response.frameCount)
    }

    @Test
    fun `component functions work correctly`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val response = VideoResponseV1(
            id = id,
            userId = userId,
            originalVideoPath = "component.mp4",
            zipPath = "comp.zip",
            frameCount = 60,
            fileSize = 1024L,
            status = VideoProcessStatusEnum.FINISHED,
            uploadedAt = uploadedAt,
            message = "Done"
        )

        val (extractedId, extractedUserId, extractedPath, extractedZip,
             extractedFrames, extractedSize, extractedStatus,
             extractedUploadedAt, extractedMessage) = response

        assertEquals(id, extractedId)
        assertEquals(userId, extractedUserId)
        assertEquals("component.mp4", extractedPath)
        assertEquals("comp.zip", extractedZip)
        assertEquals(60, extractedFrames)
        assertEquals(1024L, extractedSize)
        assertEquals(VideoProcessStatusEnum.FINISHED, extractedStatus)
        assertEquals(uploadedAt, extractedUploadedAt)
        assertEquals("Done", extractedMessage)
    }

    @Test
    fun `handles different file path formats`() {
        val pathFormats = listOf(
            "simple.mp4",
            "path/to/video.avi",
            "/absolute/path/video.mov",
            "C:\\Windows\\Path\\video.wmv",
            "video with spaces.mp4",
            "特殊字符视频.mp4"
        )

        pathFormats.forEach { path ->
            val response = VideoResponseV1(
                originalVideoPath = path,
                status = VideoProcessStatusEnum.UPLOADED
            )

            assertEquals(path, response.originalVideoPath)
        }
    }
}
