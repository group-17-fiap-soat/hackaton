package hackaton.fiapx.adapters.datasource

import hackaton.fiapx.commons.dao.VideoDAO
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VideoDataSourceImplTest {

    private lateinit var videoDataSource: VideoDataSourceImpl

    @BeforeEach
    fun setup() {
        videoDataSource = VideoDataSourceImpl()
    }

    @Test
    fun `findAll returns empty list when no videos stored`() {
        val result = videoDataSource.findAll()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `save stores video and returns same instance`() {
        val videoId = UUID.randomUUID()
        val videoDao = VideoDAO(
            id = videoId,
            userId = UUID.randomUUID(),
            originalVideoPath = "test.mp4",
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = OffsetDateTime.now(),
            fileSize = 1024L
        )

        val result = videoDataSource.save(videoDao)

        assertEquals(videoDao, result)
        assertEquals(videoId, result.id)
        assertEquals("test.mp4", result.originalVideoPath)
    }

    @Test
    fun `save throws exception when video ID is null`() {
        val videoDao = VideoDAO(
            id = null,
            userId = UUID.randomUUID(),
            originalVideoPath = "test.mp4",
            status = VideoProcessStatusEnum.UPLOADED
        )

        assertThrows<IllegalArgumentException> {
            videoDataSource.save(videoDao)
        }
    }

    @Test
    fun `findById returns null when video does not exist`() {
        val videoId = UUID.randomUUID()

        val result = videoDataSource.findById(videoId)

        assertNull(result)
    }

    @Test
    fun `findById returns video when it exists`() {
        val videoId = UUID.randomUUID()
        val videoDao = VideoDAO(
            id = videoId,
            userId = UUID.randomUUID(),
            originalVideoPath = "existing.mp4",
            status = VideoProcessStatusEnum.PROCESSING,
            fileSize = 2048L
        )

        videoDataSource.save(videoDao)
        val result = videoDataSource.findById(videoId)

        assertEquals(videoDao, result)
        assertEquals(videoId, result!!.id)
        assertEquals("existing.mp4", result.originalVideoPath)
        assertEquals(VideoProcessStatusEnum.PROCESSING, result.status)
    }

    @Test
    fun `findAll returns all stored videos`() {
        val video1Id = UUID.randomUUID()
        val video2Id = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val video1 = VideoDAO(
            id = video1Id,
            userId = userId,
            originalVideoPath = "video1.mp4",
            status = VideoProcessStatusEnum.UPLOADED,
            fileSize = 1024L
        )

        val video2 = VideoDAO(
            id = video2Id,
            userId = userId,
            originalVideoPath = "video2.mp4",
            status = VideoProcessStatusEnum.FINISHED,
            fileSize = 2048L,
            zipPath = "frames.zip",
            frameCount = 120
        )

        videoDataSource.save(video1)
        videoDataSource.save(video2)

        val result = videoDataSource.findAll()

        assertEquals(2, result.size)
        assertTrue(result.contains(video1))
        assertTrue(result.contains(video2))
    }

    @Test
    fun `save overwrites existing video with same ID`() {
        val videoId = UUID.randomUUID()
        val originalVideo = VideoDAO(
            id = videoId,
            userId = UUID.randomUUID(),
            originalVideoPath = "original.mp4",
            status = VideoProcessStatusEnum.UPLOADED,
            fileSize = 1024L
        )

        val updatedVideo = VideoDAO(
            id = videoId,
            userId = UUID.randomUUID(),
            originalVideoPath = "updated.mp4",
            status = VideoProcessStatusEnum.FINISHED,
            fileSize = 2048L,
            zipPath = "frames.zip",
            frameCount = 60
        )

        videoDataSource.save(originalVideo)
        videoDataSource.save(updatedVideo)

        val result = videoDataSource.findById(videoId)
        val allVideos = videoDataSource.findAll()

        assertEquals(updatedVideo, result)
        assertEquals("updated.mp4", result!!.originalVideoPath)
        assertEquals(VideoProcessStatusEnum.FINISHED, result.status)
        assertEquals("frames.zip", result.zipPath)
        assertEquals(60, result.frameCount)
        assertEquals(1, allVideos.size)
    }

    @Test
    fun `concurrent access handles multiple videos safely`() {
        val video1Id = UUID.randomUUID()
        val video2Id = UUID.randomUUID()
        val video3Id = UUID.randomUUID()

        val videos = listOf(
            VideoDAO(id = video1Id, status = VideoProcessStatusEnum.UPLOADED),
            VideoDAO(id = video2Id, status = VideoProcessStatusEnum.PROCESSING),
            VideoDAO(id = video3Id, status = VideoProcessStatusEnum.FINISHED)
        )

        videos.forEach { videoDataSource.save(it) }

        assertEquals(3, videoDataSource.findAll().size)
        assertEquals(VideoProcessStatusEnum.UPLOADED, videoDataSource.findById(video1Id)!!.status)
        assertEquals(VideoProcessStatusEnum.PROCESSING, videoDataSource.findById(video2Id)!!.status)
        assertEquals(VideoProcessStatusEnum.FINISHED, videoDataSource.findById(video3Id)!!.status)
    }

    @Test
    fun `save handles video with all null optional fields`() {
        val videoId = UUID.randomUUID()
        val minimalVideo = VideoDAO(
            id = videoId,
            userId = null,
            originalVideoPath = null,
            zipPath = null,
            frameCount = null,
            fileSize = null,
            status = VideoProcessStatusEnum.ERROR,
            uploadedAt = null
        )

        val result = videoDataSource.save(minimalVideo)

        assertEquals(minimalVideo, result)
        assertEquals(videoId, result.id)
        assertNull(result.userId)
        assertNull(result.originalVideoPath)
        assertEquals(VideoProcessStatusEnum.ERROR, result.status)
    }

    @Test
    fun `findAll maintains insertion order consistency`() {
        val videos = (1..5).map { i ->
            VideoDAO(
                id = UUID.randomUUID(),
                originalVideoPath = "video$i.mp4",
                status = VideoProcessStatusEnum.UPLOADED
            )
        }

        videos.forEach { videoDataSource.save(it) }
        val result = videoDataSource.findAll()

        assertEquals(5, result.size)
        videos.forEach { video ->
            assertTrue(result.any { it.id == video.id })
        }
    }
}
