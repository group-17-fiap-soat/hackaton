package hackaton.fiapx.adapters.gateways

import hackaton.fiapx.adapters.presenters.VideoMapper
import hackaton.fiapx.commons.interfaces.datasource.VideoDataSource
import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.Video
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class VideoGateway(
    private val videoDataSource: VideoDataSource
) : VideoGatewayInterface {

    override fun listAll(): List<Video> {
        return videoDataSource.findAll().map(VideoMapper::toEntity)
    }

    override fun findById(videoId: UUID): Video? {
        return videoDataSource.findById(videoId)?.let(VideoMapper::toEntity)
    }

    override fun save(updatedVideo: Video): Video {
        val videoDao = VideoMapper.toDAO(updatedVideo)
        return VideoMapper.fromDaoToEntity(videoDataSource.save(videoDao))
    }
}