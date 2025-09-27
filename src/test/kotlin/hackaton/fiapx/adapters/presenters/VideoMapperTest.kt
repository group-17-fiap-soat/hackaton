package hackaton.fiapx.adapters.presenters

import hackaton.fiapx.commons.dao.VideoDAO
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.entities.Video
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VideoMapperTest {

    @Test
    fun `toEntity converts VideoDAO to Video entity correctly`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()
        val dao = VideoDAO(
            id = videoId,
            userId = userId,
            originalVideoPath = "test.mp4",
            zipPath = "frames.zip",
            frameCount = 120,
            fileSize = 1024L,
            status = VideoProcessStatusEnum.FINISHED,
            uploadedAt = uploadedAt
        )

        val entity = VideoMapper.toEntity(dao)

        assertEquals(videoId, entity.id)
        assertEquals(userId, entity.userId)
        assertEquals("test.mp4", entity.originalVideoPath)
        assertEquals("frames.zip", entity.zipPath)
        assertEquals(120, entity.frameCount)
        assertEquals(1024L, entity.fileSize)
        assertEquals(VideoProcessStatusEnum.FINISHED, entity.status)
        assertEquals(uploadedAt, entity.uploadedAt)
    }

    @Test
    fun `toDAO converts Video entity to VideoDAO correctly`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()
        val entity = Video(
            id = videoId,
            userId = userId,
            originalVideoPath = "video.mp4",
            zipPath = "output.zip",
            frameCount = 60,
            fileSize = 2048L,
            status = VideoProcessStatusEnum.PROCESSING,
            uploadedAt = uploadedAt
        )

        val dao = VideoMapper.toDAO(entity)

        assertEquals(videoId, dao.id)
        assertEquals(userId, dao.userId)
        assertEquals("video.mp4", dao.originalVideoPath)
        assertEquals("output.zip", dao.zipPath)
        assertEquals(60, dao.frameCount)
        assertEquals(2048L, dao.fileSize)
        assertEquals(VideoProcessStatusEnum.PROCESSING, dao.status)
        assertEquals(uploadedAt, dao.uploadedAt)
    }

    @Test
    fun `fromDaoToEntity converts VideoDAO to Video entity correctly`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()
        val dao = VideoDAO(
            id = videoId,
            userId = userId,
            originalVideoPath = "sample.mp4",
            zipPath = null,
            frameCount = null,
            fileSize = 512L,
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = uploadedAt
        )

        val entity = VideoMapper.fromDaoToEntity(dao)

        assertEquals(videoId, entity.id)
        assertEquals(userId, entity.userId)
        assertEquals("sample.mp4", entity.originalVideoPath)
        assertEquals(null, entity.zipPath)
        assertEquals(null, entity.frameCount)
        assertEquals(512L, entity.fileSize)
        assertEquals(VideoProcessStatusEnum.UPLOADED, entity.status)
        assertEquals(uploadedAt, entity.uploadedAt)
    }

    @Test
    fun `toVideoResponseV1 converts Video entity to VideoResponseV1 correctly`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()
        val entity = Video(
            id = videoId,
            userId = userId,
            originalVideoPath = "response.mp4",
            zipPath = "response.zip",
            frameCount = 90,
            fileSize = 4096L,
            status = VideoProcessStatusEnum.FINISHED,
            uploadedAt = uploadedAt,
            errorMessage = "Processing completed successfully"
        )

        val response = VideoMapper.toVideoResponseV1(entity)

        assertEquals(videoId, response.id)
        assertEquals(userId, response.userId)
        assertEquals("response.mp4", response.originalVideoPath)
        assertEquals("response.zip", response.zipPath)
        assertEquals(90, response.frameCount)
        assertEquals(4096L, response.fileSize)
        assertEquals(VideoProcessStatusEnum.FINISHED, response.status)
        assertEquals(uploadedAt, response.uploadedAt)
        assertEquals("Processing completed successfully", response.message)
    }

    @Test
    fun `toVideoResponseV1 handles null values correctly`() {
        val videoId = UUID.randomUUID()
        val entity = Video(
            id = videoId,
            userId = null,
            originalVideoPath = null,
            zipPath = null,
            frameCount = null,
            fileSize = null,
            status = VideoProcessStatusEnum.ERROR,
            uploadedAt = null,
            errorMessage = null
        )

        val response = VideoMapper.toVideoResponseV1(entity)

        assertEquals(videoId, response.id)
        assertEquals(null, response.userId)
        assertEquals(null, response.originalVideoPath)
        assertEquals(null, response.zipPath)
        assertEquals(null, response.frameCount)
        assertEquals(null, response.fileSize)
        assertEquals(VideoProcessStatusEnum.ERROR, response.status)
        assertEquals(null, response.uploadedAt)
        assertEquals(null, response.message)
    }

    @Test
    fun `toEntity handles all VideoProcessStatusEnum values`() {
        val statuses = VideoProcessStatusEnum.values()

        statuses.forEach { status ->
            val dao = VideoDAO(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                status = status
            )

            val entity = VideoMapper.toEntity(dao)

            assertEquals(status, entity.status)
        }
    }

    @Test
    fun `mapping preserves UUID values across conversions`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val originalEntity = Video(
            id = videoId,
            userId = userId,
            status = VideoProcessStatusEnum.UPLOADED
        )

        val dao = VideoMapper.toDAO(originalEntity)
        val convertedEntity = VideoMapper.fromDaoToEntity(dao)

        assertEquals(videoId, convertedEntity.id)
        assertEquals(userId, convertedEntity.userId)
    }
}
