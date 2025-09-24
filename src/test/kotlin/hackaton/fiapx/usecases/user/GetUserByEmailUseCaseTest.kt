package hackaton.fiapx.usecases.user

import hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface
import hackaton.fiapx.entities.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class GetUserByEmailUseCaseTest {

    private class FakeUserGateway(private val user: User?) : UserGatewayInterface {
        override fun findById(id: UUID): User? = null
        override fun findByEmail(email: String): User? = user
        override fun save(entity: User): User = entity
    }

    @Test
    fun returnsUserWhenFound() {
        val expected = User(email = "a@b.com")
        val useCase = GetUserByEmailUseCase(FakeUserGateway(expected))
        assertEquals(expected, useCase.execute("a@b.com"))
    }

    @Test
    fun returnsNullWhenNotFound() {
        val useCase = GetUserByEmailUseCase(FakeUserGateway(null))
        org.junit.jupiter.api.Assertions.assertNull(useCase.execute("x@y.com"))
    }
}
