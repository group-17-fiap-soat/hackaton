package hackaton.fiapx.commons.interfaces.gateways

import hackaton.fiapx.entities.Video
import java.util.UUID

interface VideoGatewayInterface {
    fun findById(videoId: UUID): Video?
    fun save(updatedVideo: Video): Video
    fun listAll(): List<Video>
}