package hackaton.fiapx.usecases.process

import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.Video
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ListVideoUseCaseTest {

    private class FakeVideoGateway(private val list: List<Video>) : VideoGatewayInterface {
        override fun listAll(): List<Video> = list
        override fun save(entity: Video): Video = entity
    }

    @Test
    fun returnsEmptyListWhenNoVideos() {
        val useCase = ListVideoUseCase(FakeVideoGateway(emptyList()))
        assertEquals(emptyList<Video>(), useCase.execute())
    }

    @Test
    fun returnsAllVideosFromGateway() {
        val videos = listOf(Video(originalVideoPath = "a.mp4"), Video(originalVideoPath = "b.mp4"))
        val useCase = ListVideoUseCase(FakeVideoGateway(videos))
        assertEquals(videos, useCase.execute())
    }
}

