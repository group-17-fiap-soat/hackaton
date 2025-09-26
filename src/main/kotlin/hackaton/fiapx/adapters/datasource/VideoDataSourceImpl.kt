package hackaton.fiapx.adapters.datasource

import hackaton.fiapx.commons.dao.VideoDAO
import hackaton.fiapx.commons.interfaces.datasource.VideoDataSource
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class VideoDataSourceImpl : VideoDataSource {

    private val videoStorage = ConcurrentHashMap<UUID, VideoDAO>()

    override fun findAll(): List<VideoDAO> {
        return videoStorage.values.toList()
    }

    override fun findById(videoId: UUID): VideoDAO? {
        return videoStorage[videoId]
    }

    override fun save(videoDao: VideoDAO): VideoDAO {
        val id = videoDao.id ?: throw IllegalArgumentException("Video ID cannot be null")
        videoStorage[id] = videoDao
        return videoDao
    }
}