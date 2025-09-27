package hackaton.fiapx.adapters.gateways

import hackaton.fiapx.commons.dao.VideoDAO
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.commons.interfaces.datasource.VideoDataSource
import hackaton.fiapx.entities.Video
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VideoGatewayTest {

    private lateinit var videoDataSource: VideoDataSource
    private lateinit var videoGateway: VideoGateway

    @BeforeEach
    fun setup() {
        videoDataSource = mock()
        videoGateway = VideoGateway(videoDataSource)
    }

    @Test
    fun `listAll returns empty list when no videos exist`() {
        whenever(videoDataSource.findAll()).thenReturn(emptyList())

        val result = videoGateway.listAll()

        assertEquals(0, result.size)
        verify(videoDataSource).findAll()
    }

    @Test
    fun `listAll returns mapped videos when videos exist`() {
        val videoId1 = UUID.randomUUID()
        val videoId2 = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val daos = listOf(
            VideoDAO(
                id = videoId1,
                userId = userId,
                originalVideoPath = "video1.mp4",
                status = VideoProcessStatusEnum.UPLOADED,
                uploadedAt = uploadedAt,
                fileSize = 1024L
            ),
            VideoDAO(
                id = videoId2,
                userId = userId,
                originalVideoPath = "video2.mp4",
                status = VideoProcessStatusEnum.FINISHED,
                uploadedAt = uploadedAt,
                fileSize = 2048L,
                zipPath = "frames.zip",
                frameCount = 120
            )
        )

        whenever(videoDataSource.findAll()).thenReturn(daos)

        val result = videoGateway.listAll()

        assertEquals(2, result.size)
        assertEquals(videoId1, result[0].id)
        assertEquals("video1.mp4", result[0].originalVideoPath)
        assertEquals(VideoProcessStatusEnum.UPLOADED, result[0].status)
        assertEquals(videoId2, result[1].id)
        assertEquals("video2.mp4", result[1].originalVideoPath)
        assertEquals(VideoProcessStatusEnum.FINISHED, result[1].status)
        assertEquals("frames.zip", result[1].zipPath)
        assertEquals(120, result[1].frameCount)
    }

    @Test
    fun `findById returns null when video does not exist`() {
        val videoId = UUID.randomUUID()
        whenever(videoDataSource.findById(videoId)).thenReturn(null)

        val result = videoGateway.findById(videoId)

        assertNull(result)
        verify(videoDataSource).findById(videoId)
    }

    @Test
    fun `findById returns mapped video when video exists`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val dao = VideoDAO(
            id = videoId,
            userId = userId,
            originalVideoPath = "found.mp4",
            status = VideoProcessStatusEnum.PROCESSING,
            uploadedAt = uploadedAt,
            fileSize = 512L
        )

        whenever(videoDataSource.findById(videoId)).thenReturn(dao)

        val result = videoGateway.findById(videoId)

        assertEquals(videoId, result!!.id)
        assertEquals(userId, result.userId)
        assertEquals("found.mp4", result.originalVideoPath)
        assertEquals(VideoProcessStatusEnum.PROCESSING, result.status)
        assertEquals(512L, result.fileSize)
    }

    @Test
    fun `save converts entity to dao, saves it, and returns mapped result`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val inputEntity = Video(
            id = videoId,
            userId = userId,
            originalVideoPath = "input.mp4",
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = uploadedAt,
            fileSize = 1536L
        )

        val savedDao = VideoDAO(
            id = videoId,
            userId = userId,
            originalVideoPath = "input.mp4",
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = uploadedAt,
            fileSize = 1536L
        )

        whenever(videoDataSource.save(any<VideoDAO>())).thenReturn(savedDao)

        val result = videoGateway.save(inputEntity)

        assertEquals(videoId, result.id)
        assertEquals(userId, result.userId)
        assertEquals("input.mp4", result.originalVideoPath)
        assertEquals(VideoProcessStatusEnum.UPLOADED, result.status)
        assertEquals(1536L, result.fileSize)

        val daoCaptor = argumentCaptor<VideoDAO>()
        verify(videoDataSource).save(daoCaptor.capture())
        assertEquals(videoId, daoCaptor.firstValue.id)
        assertEquals(userId, daoCaptor.firstValue.userId)
    }

    @Test
    fun `save handles video with all fields populated`() {
        val videoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val uploadedAt = OffsetDateTime.now()

        val fullEntity = Video(
            id = videoId,
            userId = userId,
            originalVideoPath = "complete.mp4",
            zipPath = "complete.zip",
            frameCount = 180,
            fileSize = 3072L,
            status = VideoProcessStatusEnum.FINISHED,
            uploadedAt = uploadedAt,
            errorMessage = "Success"
        )

        val savedDao = VideoDAO(
            id = videoId,
            userId = userId,
            originalVideoPath = "complete.mp4",
            zipPath = "complete.zip",
            frameCount = 180,
            fileSize = 3072L,
            status = VideoProcessStatusEnum.FINISHED,
            uploadedAt = uploadedAt
        )

        whenever(videoDataSource.save(any<VideoDAO>())).thenReturn(savedDao)

        val result = videoGateway.save(fullEntity)

        assertEquals(videoId, result.id)
        assertEquals("complete.zip", result.zipPath)
        assertEquals(180, result.frameCount)
        assertEquals(VideoProcessStatusEnum.FINISHED, result.status)
    }

    @Test
    fun `save handles video with null optional fields`() {
        val videoId = UUID.randomUUID()
        val minimalEntity = Video(
            id = videoId,
            userId = null,
            originalVideoPath = null,
            zipPath = null,
            frameCount = null,
            fileSize = null,
            status = VideoProcessStatusEnum.ERROR,
            uploadedAt = null
        )

        val savedDao = VideoDAO(
            id = videoId,
            userId = null,
            originalVideoPath = null,
            zipPath = null,
            frameCount = null,
            fileSize = null,
            status = VideoProcessStatusEnum.ERROR,
            uploadedAt = null
        )

        whenever(videoDataSource.save(any<VideoDAO>())).thenReturn(savedDao)

        val result = videoGateway.save(minimalEntity)

        assertEquals(videoId, result.id)
        assertNull(result.userId)
        assertNull(result.originalVideoPath)
        assertNull(result.zipPath)
        assertNull(result.frameCount)
        assertEquals(VideoProcessStatusEnum.ERROR, result.status)
    }
}
