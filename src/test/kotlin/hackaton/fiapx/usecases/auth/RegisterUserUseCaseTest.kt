package hackaton.fiapx.usecases.auth

import hackaton.fiapx.commons.exception.UserAlreadyExistsException
import hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface
import hackaton.fiapx.entities.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class RegisterUserUseCaseTest {

    private class FakeUserGateway : UserGatewayInterface {
        var saved: User? = null
        var existing: User? = null
        override fun findById(id: UUID): User? = null
        override fun findByEmail(email: String): User? = existing
        override fun save(entity: User): User { saved = entity; return entity }
    }

    @Test
    fun throwsWhenUserAlreadyExists() {
        val fake = FakeUserGateway().apply { existing = User(email = "a@b.com") }
        val useCase = RegisterUserUseCase(fake)

        val ex = assertThrows(UserAlreadyExistsException::class.java) {
            useCase.execute(User(email = "a@b.com"))
        }
        assertTrue(ex.message!!.contains("a@b.com"))
        assertNull(fake.saved)
    }

    @Test
    fun savesUserWhenNotExists() {
        val fake = FakeUserGateway()
        val useCase = RegisterUserUseCase(fake)

        val user = User(email = "new@b.com")
        useCase.execute(user)

        assertEquals(user, fake.saved)
    }
}

