package hackaton.fiapx.adapters.gateways

import hackaton.fiapx.commons.dao.VideoDAO
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.commons.interfaces.datasource.VideoDataSource
import hackaton.fiapx.entities.Video
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Function

class VideoGatewayTest {

    private class FakeVideoDataSource : VideoDataSource {
        var savedVideo: VideoDAO? = null
        var videosToReturn: List<VideoDAO> = emptyList()

        override fun findAll(): MutableList<VideoDAO> {
            return videosToReturn.toMutableList()
        }

        override fun <S : VideoDAO> save(entity: S): S {
            savedVideo = entity
            return entity
        }

        // Minimal JpaRepository method implementations for compilation
        override fun findById(id: UUID): Optional<VideoDAO> = Optional.empty()
        override fun findAll(sort: Sort): MutableList<VideoDAO> = mutableListOf()
        override fun findAllById(ids: MutableIterable<UUID>): MutableList<VideoDAO> = mutableListOf()
        override fun <S : VideoDAO> saveAll(entities: MutableIterable<S>): MutableList<S> = mutableListOf()
        override fun existsById(id: UUID): Boolean = false
        override fun count(): Long = 0
        override fun deleteById(id: UUID) {}
        override fun delete(entity: VideoDAO) {}
        override fun deleteAllById(ids: MutableIterable<UUID>) {}
        override fun deleteAll(entities: MutableIterable<VideoDAO>) {}
        override fun deleteAll() {}
        override fun flush() {}
        override fun <S : VideoDAO> saveAndFlush(entity: S): S = entity
        override fun <S : VideoDAO> saveAllAndFlush(entities: MutableIterable<S>): MutableList<S> = mutableListOf()
        override fun deleteAllInBatch(entities: MutableIterable<VideoDAO>) {}
        override fun deleteAllByIdInBatch(ids: MutableIterable<UUID>) {}
        override fun deleteAllInBatch() {}
        override fun getOne(id: UUID): VideoDAO = VideoDAO()
        override fun getById(id: UUID): VideoDAO = VideoDAO()
        override fun getReferenceById(id: UUID): VideoDAO = VideoDAO()
        override fun findAll(pageable: Pageable): Page<VideoDAO> = Page.empty()
        override fun <S : VideoDAO> findOne(example: Example<S>): Optional<S> = Optional.empty()
        override fun <S : VideoDAO> findAll(example: Example<S>): MutableList<S> = mutableListOf()
        override fun <S : VideoDAO> findAll(example: Example<S>, sort: Sort): MutableList<S> = mutableListOf()
        override fun <S : VideoDAO> findAll(example: Example<S>, pageable: Pageable): Page<S> = Page.empty()
        override fun <S : VideoDAO> count(example: Example<S>): Long = 0
        override fun <S : VideoDAO> exists(example: Example<S>): Boolean = false
        override fun <S : VideoDAO, R : Any> findBy(example: Example<S>, queryFunction: Function<FluentQuery.FetchableFluentQuery<S>, R>): R {
            throw UnsupportedOperationException()
        }
    }

    @Test
    fun listAllReturnsEmptyListWhenNoVideos() {
        val dataSource = FakeVideoDataSource()
        val gateway = VideoGateway(dataSource)

        val result = gateway.listAll()

        assertTrue(result.isEmpty())
    }

    @Test
    fun listAllReturnsAllVideosFromDataSource() {
        val video1 = VideoDAO(UUID.randomUUID(), UUID.randomUUID(), "path1.mp4", null, null, 1000L, VideoProcessStatusEnum.SUCCESS, OffsetDateTime.now())
        val video2 = VideoDAO(UUID.randomUUID(), UUID.randomUUID(), "path2.mp4", "frames.zip", 10, 2000L, VideoProcessStatusEnum.PROCESSING, OffsetDateTime.now())
        val dataSource = FakeVideoDataSource().apply { videosToReturn = listOf(video1, video2) }
        val gateway = VideoGateway(dataSource)

        val result = gateway.listAll()

        assertEquals(2, result.size)
        assertEquals("path1.mp4", result[0].originalVideoPath)
        assertEquals("path2.mp4", result[1].originalVideoPath)
        assertEquals(VideoProcessStatusEnum.SUCCESS, result[0].status)
        assertEquals(VideoProcessStatusEnum.PROCESSING, result[1].status)
    }

    @Test
    fun saveCreatesNewVideoWithGeneratedId() {
        val dataSource = FakeVideoDataSource()
        val gateway = VideoGateway(dataSource)
        val video = Video(null, UUID.randomUUID(), "new-video.mp4", null, null, 5000L, VideoProcessStatusEnum.PROCESSING)

        val result = gateway.save(video)

        assertEquals("new-video.mp4", result.originalVideoPath)
        assertEquals(5000L, result.fileSize)
        assertEquals(VideoProcessStatusEnum.PROCESSING, result.status)
        org.junit.jupiter.api.Assertions.assertNotNull(dataSource.savedVideo)
    }

    @Test
    fun saveUpdatesExistingVideo() {
        val existingId = UUID.randomUUID()
        val dataSource = FakeVideoDataSource()
        val gateway = VideoGateway(dataSource)
        val video = Video(existingId, UUID.randomUUID(), "updated-video.mp4", "frames.zip", 15, 3000L, VideoProcessStatusEnum.SUCCESS)

        val result = gateway.save(video)

        assertEquals(existingId, result.id)
        assertEquals("updated-video.mp4", result.originalVideoPath)
        assertEquals("frames.zip", result.zipPath)
        assertEquals(15, result.frameCount)
        assertEquals(VideoProcessStatusEnum.SUCCESS, result.status)
        org.junit.jupiter.api.Assertions.assertNotNull(dataSource.savedVideo)
        assertEquals(existingId, dataSource.savedVideo?.id)
    }

    @Test
    fun saveHandlesVideoWithNullFields() {
        val dataSource = FakeVideoDataSource()
        val gateway = VideoGateway(dataSource)
        val video = Video(null, null, null, null, null, null, null)

        val result = gateway.save(video)

        org.junit.jupiter.api.Assertions.assertNull(result.userId)
        org.junit.jupiter.api.Assertions.assertNull(result.originalVideoPath)
        org.junit.jupiter.api.Assertions.assertNull(result.zipPath)
        org.junit.jupiter.api.Assertions.assertNull(result.frameCount)
        org.junit.jupiter.api.Assertions.assertNull(result.fileSize)
        org.junit.jupiter.api.Assertions.assertNull(result.status)
        org.junit.jupiter.api.Assertions.assertNotNull(dataSource.savedVideo)
    }

    @Test
    fun saveHandlesVideoWithCompleteData() {
        val dataSource = FakeVideoDataSource()
        val gateway = VideoGateway(dataSource)
        val userId = UUID.randomUUID()
        val video = Video(null, userId, "complete-video.mp4", "complete-frames.zip", 20, 10000L, VideoProcessStatusEnum.SUCCESS, OffsetDateTime.now())

        val result = gateway.save(video)

        assertEquals(userId, result.userId)
        assertEquals("complete-video.mp4", result.originalVideoPath)
        assertEquals("complete-frames.zip", result.zipPath)
        assertEquals(20, result.frameCount)
        assertEquals(10000L, result.fileSize)
        assertEquals(VideoProcessStatusEnum.SUCCESS, result.status)
        org.junit.jupiter.api.Assertions.assertNotNull(result.uploadedAt)
    }

    @Test
    fun listAllHandlesLargeNumberOfVideos() {
        val videos = (1..100).map { i ->
            VideoDAO(UUID.randomUUID(), UUID.randomUUID(), "video$i.mp4", null, null, i * 1000L, VideoProcessStatusEnum.SUCCESS, OffsetDateTime.now())
        }
        val dataSource = FakeVideoDataSource().apply { videosToReturn = videos }
        val gateway = VideoGateway(dataSource)

        val result = gateway.listAll()

        assertEquals(100, result.size)
        assertEquals("video1.mp4", result[0].originalVideoPath)
        assertEquals("video100.mp4", result[99].originalVideoPath)
        assertEquals(1000L, result[0].fileSize)
        assertEquals(100000L, result[99].fileSize)
    }
}
