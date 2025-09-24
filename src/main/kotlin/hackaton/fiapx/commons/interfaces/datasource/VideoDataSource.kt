package hackaton.fiapx.commons.interfaces.datasource

import hackaton.fiapx.commons.dao.VideoDAO
import java.util.UUID

interface VideoDataSource {
    fun findAll(): List<VideoDAO>
    fun findById(videoId: UUID): VideoDAO?
    fun save(videoDao: VideoDAO): VideoDAO
}