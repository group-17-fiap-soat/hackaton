package hackaton.fiapx.commons.dao

import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class VideoDAOTest {

    @Test
    fun `creates video DAO with all properties`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val videoDAO = VideoDAO(
            id = id,
            userId = userId,
            originalVideoPath = "/uploads/video.mp4",
            zipPath = "/outputs/frames.zip",
            frameCount = 120,
            fileSize = 1048576L,
            status = VideoProcessStatusEnum.FINISHED,
            uploadedAt = uploadedAt
        )

        assertEquals(id, videoDAO.id)
        assertEquals(userId, videoDAO.userId)
        assertEquals("/uploads/video.mp4", videoDAO.originalVideoPath)
        assertEquals("/outputs/frames.zip", videoDAO.zipPath)
        assertEquals(120, videoDAO.frameCount)
        assertEquals(1048576L, videoDAO.fileSize)
        assertEquals(VideoProcessStatusEnum.FINISHED, videoDAO.status)
        assertEquals(uploadedAt, videoDAO.uploadedAt)
    }

    @Test
    fun `creates video DAO with minimal properties`() {
        val videoDAO = VideoDAO()

        assertNull(videoDAO.id)
        assertNull(videoDAO.userId)
        assertNull(videoDAO.originalVideoPath)
        assertNull(videoDAO.zipPath)
        assertNull(videoDAO.frameCount)
        assertNull(videoDAO.fileSize)
        assertNull(videoDAO.status)
        assertNull(videoDAO.uploadedAt)
    }

    @Test
    fun `creates video DAO for uploaded state`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val videoDAO = VideoDAO(
            id = id,
            userId = userId,
            originalVideoPath = "/uploads/new-upload.mp4",
            fileSize = 2048000L,
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = uploadedAt
        )

        assertEquals(VideoProcessStatusEnum.UPLOADED, videoDAO.status)
        assertNull(videoDAO.zipPath)
        assertNull(videoDAO.frameCount)
        assertNotNull(videoDAO.uploadedAt)
    }

    @Test
    fun `creates video DAO for processing state`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val videoDAO = VideoDAO(
            id = id,
            userId = userId,
            originalVideoPath = "/uploads/processing.mp4",
            fileSize = 5242880L,
            status = VideoProcessStatusEnum.PROCESSING
        )

        assertEquals(VideoProcessStatusEnum.PROCESSING, videoDAO.status)
        assertNull(videoDAO.zipPath)
        assertNull(videoDAO.frameCount)
    }

    @Test
    fun `creates video DAO for finished state with results`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val videoDAO = VideoDAO(
            id = id,
            userId = userId,
            originalVideoPath = "/uploads/completed.mp4",
            zipPath = "/outputs/completed-frames.zip",
            frameCount = 300,
            fileSize = 10485760L,
            status = VideoProcessStatusEnum.FINISHED,
            uploadedAt = uploadedAt
        )

        assertEquals(VideoProcessStatusEnum.FINISHED, videoDAO.status)
        assertEquals("/outputs/completed-frames.zip", videoDAO.zipPath)
        assertEquals(300, videoDAO.frameCount)
        assertEquals(10485760L, videoDAO.fileSize)
    }

    @Test
    fun `creates video DAO for error state`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val videoDAO = VideoDAO(
            id = id,
            userId = userId,
            originalVideoPath = "/uploads/failed.mp4",
            fileSize = 1024L,
            status = VideoProcessStatusEnum.ERROR
        )

        assertEquals(VideoProcessStatusEnum.ERROR, videoDAO.status)
        assertNull(videoDAO.zipPath)
        assertNull(videoDAO.frameCount)
    }

    @Test
    fun `maintains data class equality and hashCode`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val dao1 = VideoDAO(
            id = id,
            userId = userId,
            originalVideoPath = "test.mp4",
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = uploadedAt
        )

        val dao2 = VideoDAO(
            id = id,
            userId = userId,
            originalVideoPath = "test.mp4",
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = uploadedAt
        )

        val dao3 = VideoDAO(
            id = UUID.randomUUID(),
            userId = userId,
            originalVideoPath = "test.mp4",
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = uploadedAt
        )

        assertEquals(dao1, dao2)
        assertEquals(dao1.hashCode(), dao2.hashCode())
        assert(dao1 != dao3)
    }

    @Test
    fun `handles different file path formats`() {
        val pathFormats = listOf(
            "/absolute/unix/path/video.mp4",
            "C:\\Windows\\Path\\video.wmv",
            "relative/path/video.avi",
            "./current/dir/video.mov",
            "../parent/dir/video.mkv"
        )

        pathFormats.forEach { path ->
            val videoDAO = VideoDAO(
                id = UUID.randomUUID(),
                originalVideoPath = path,
                status = VideoProcessStatusEnum.UPLOADED
            )

            assertEquals(path, videoDAO.originalVideoPath)
        }
    }

    @Test
    fun `handles various file sizes`() {
        val fileSizes = listOf(
            0L,                    // Empty file
            1024L,                 // 1KB
            1048576L,              // 1MB
            1073741824L,           // 1GB
            10737418240L           // 10GB
        )

        fileSizes.forEach { size ->
            val videoDAO = VideoDAO(
                id = UUID.randomUUID(),
                fileSize = size,
                status = VideoProcessStatusEnum.UPLOADED
            )

            assertEquals(size, videoDAO.fileSize)
        }
    }

    @Test
    fun `handles different frame counts`() {
        val frameCounts = listOf(0, 1, 30, 60, 1800, 7200) // Various durations

        frameCounts.forEach { count ->
            val videoDAO = VideoDAO(
                id = UUID.randomUUID(),
                frameCount = count,
                status = VideoProcessStatusEnum.FINISHED
            )

            assertEquals(count, videoDAO.frameCount)
        }
    }

    @Test
    fun `supports all video process status enum values`() {
        val statuses = VideoProcessStatusEnum.values()

        statuses.forEach { status ->
            val videoDAO = VideoDAO(
                id = UUID.randomUUID(),
                status = status
            )

            assertEquals(status, videoDAO.status)
        }
    }

    @Test
    fun `component functions work correctly`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val videoDAO = VideoDAO(
            id = id,
            userId = userId,
            originalVideoPath = "component.mp4",
            zipPath = "comp.zip",
            frameCount = 90,
            fileSize = 2048L,
            status = VideoProcessStatusEnum.FINISHED,
            uploadedAt = uploadedAt
        )

        val (extractedId, extractedUserId, extractedPath, extractedZip,
             extractedFrames, extractedSize, extractedStatus, extractedUploadedAt) = videoDAO

        assertEquals(id, extractedId)
        assertEquals(userId, extractedUserId)
        assertEquals("component.mp4", extractedPath)
        assertEquals("comp.zip", extractedZip)
        assertEquals(90, extractedFrames)
        assertEquals(2048L, extractedSize)
        assertEquals(VideoProcessStatusEnum.FINISHED, extractedStatus)
        assertEquals(uploadedAt, extractedUploadedAt)
    }

    @Test
    fun `handles timestamp precision`() {
        val preciseTimestamp = OffsetDateTime.now()

        val videoDAO = VideoDAO(
            id = UUID.randomUUID(),
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = preciseTimestamp
        )

        assertEquals(preciseTimestamp, videoDAO.uploadedAt)
        assertEquals(preciseTimestamp.nano, videoDAO.uploadedAt?.nano)
    }

    @Test
    fun `copy function creates new instance with modified properties`() {
        val originalDAO = VideoDAO(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            originalVideoPath = "original.mp4",
            status = VideoProcessStatusEnum.UPLOADED
        )

        val modifiedDAO = originalDAO.copy(
            status = VideoProcessStatusEnum.FINISHED,
            zipPath = "output.zip",
            frameCount = 150
        )

        assertEquals(originalDAO.id, modifiedDAO.id)
        assertEquals(originalDAO.userId, modifiedDAO.userId)
        assertEquals(originalDAO.originalVideoPath, modifiedDAO.originalVideoPath)
        assertEquals(VideoProcessStatusEnum.FINISHED, modifiedDAO.status)
        assertEquals("output.zip", modifiedDAO.zipPath)
        assertEquals(150, modifiedDAO.frameCount)
        assertNull(originalDAO.zipPath)
        assertNull(originalDAO.frameCount)
        assertEquals(VideoProcessStatusEnum.UPLOADED, originalDAO.status)
    }
}
