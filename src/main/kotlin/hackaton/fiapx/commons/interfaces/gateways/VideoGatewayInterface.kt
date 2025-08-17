package hackaton.fiapx.commons.interfaces.gateways

import hackaton.fiapx.entities.Video

interface VideoGatewayInterface {
    fun listAll(): List<Video>
    fun save(entity: Video): Video
}