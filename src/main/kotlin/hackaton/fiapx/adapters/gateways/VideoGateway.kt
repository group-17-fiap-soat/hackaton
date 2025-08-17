package hackaton.fiapx.adapters.gateways

import hackaton.fiapx.adapters.presenters.VideoMapper
import hackaton.fiapx.commons.interfaces.datasource.VideoDataSource
import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.Video
import org.springframework.stereotype.Component

@Component
class VideoGateway(
    val videoDataSource: VideoDataSource
) : VideoGatewayInterface {

    override fun listAll(): List<Video> {
        return videoDataSource.findAll().map(VideoMapper::toEntity)
    }

    override fun save(entity: Video): Video {
        val processVideoDao = VideoMapper.toDAO(entity)
        return VideoMapper.fromDaoToEntity(videoDataSource.save(processVideoDao))
    }
}