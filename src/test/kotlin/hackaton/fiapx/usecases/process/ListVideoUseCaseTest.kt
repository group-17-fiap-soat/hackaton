package hackaton.fiapx.usecases.process

import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.User
import hackaton.fiapx.entities.Video
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListVideoUseCaseTest {

    private lateinit var videoGateway: VideoGatewayInterface
    private lateinit var useCase: ListVideoUseCase

    @BeforeEach
    fun setup() {
        videoGateway = mock(VideoGatewayInterface::class.java)
        useCase = ListVideoUseCase(videoGateway)
    }

    @Test
    fun `returns all videos for user when user has multiple videos`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId)
        val video1 = Video(id = UUID.randomUUID(), userId = userId)
        val video2 = Video(id = UUID.randomUUID(), userId = userId)
        val video3 = Video(id = UUID.randomUUID(), userId = UUID.randomUUID())

        `when`(videoGateway.listAll()).thenReturn(listOf(video1, video2, video3))

        val result = useCase.execute(user)

        assertEquals(2, result.size)
        assertTrue(result.all { it.userId == userId })
    }

    @Test
    fun `returns empty list when user has no videos`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId)
        val otherVideo = Video(id = UUID.randomUUID(), userId = UUID.randomUUID())

        `when`(videoGateway.listAll()).thenReturn(listOf(otherVideo))

        val result = useCase.execute(user)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty list when gateway returns empty list`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId)

        `when`(videoGateway.listAll()).thenReturn(emptyList())

        val result = useCase.execute(user)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns only user videos when mixed with other user videos`() {
        val userId = UUID.randomUUID()
        val otherUserId = UUID.randomUUID()
        val user = User(id = userId)

        val userVideo1 = Video(id = UUID.randomUUID(), userId = userId)
        val userVideo2 = Video(id = UUID.randomUUID(), userId = userId)
        val otherUserVideo1 = Video(id = UUID.randomUUID(), userId = otherUserId)
        val otherUserVideo2 = Video(id = UUID.randomUUID(), userId = otherUserId)

        `when`(videoGateway.listAll()).thenReturn(listOf(userVideo1, otherUserVideo1, userVideo2, otherUserVideo2))

        val result = useCase.execute(user)

        assertEquals(2, result.size)
        assertTrue(result.containsAll(listOf(userVideo1, userVideo2)))
        assertTrue(result.none { it.userId == otherUserId })
    }
}
