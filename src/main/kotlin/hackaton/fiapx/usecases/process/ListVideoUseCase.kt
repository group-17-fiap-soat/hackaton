package hackaton.fiapx.usecases.process

import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.Video
import org.springframework.stereotype.Service

@Service
class ListVideoUseCase(
    private val videoGatewayInterface: VideoGatewayInterface
) {

    fun execute(): List<Video> {
        return videoGatewayInterface.listAll()
    }
}