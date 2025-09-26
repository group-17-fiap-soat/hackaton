package hackaton.fiapx.usecases.process

import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.Video
import hackaton.fiapx.entities.User
import org.springframework.stereotype.Service
import java.util.*

@Service
class ListVideoUseCase(
    private val videoGatewayInterface: VideoGatewayInterface
) {

    fun execute(user: User): List<Video> {
        return videoGatewayInterface.listAll().filter { video ->
            video.userId == user.id
        }
    }
}