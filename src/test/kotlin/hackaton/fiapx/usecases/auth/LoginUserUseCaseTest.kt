package hackaton.fiapx.usecases.auth

import hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface
import hackaton.fiapx.entities.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import java.util.*
import javax.naming.AuthenticationException

class LoginUserUseCaseTest {

    private class FakeUserGateway(var result: User?) : UserGatewayInterface {
        override fun findById(id: UUID) = null
        override fun findByEmail(email: String) = result
        override fun save(entity: User) = entity
    }

    private class FakeAuthManager(private val shouldThrow: Boolean) : AuthenticationManager {
        override fun authenticate(authentication: Authentication?): Authentication {
            if (shouldThrow) throw AuthenticationException("bad creds")
            return UsernamePasswordAuthenticationToken(authentication?.principal, authentication?.credentials)
        }
    }

    @Test
    fun returnsUserWhenAuthenticationSucceeds() {
        val expected = User(email = "user@x.com")
        val useCase = LoginUserUseCase(FakeAuthManager(false), FakeUserGateway(expected))

        val result = useCase.execute(User(email = "user@x.com", passwordHash = "123"))

        assertEquals(expected, result)
    }

    @Test
    fun returnsNullWhenGatewayDoesNotFindUser() {
        val useCase = LoginUserUseCase(FakeAuthManager(false), FakeUserGateway(null))
        val result = useCase.execute(User(email = "none@x.com", passwordHash = "123"))
        assertNull(result)
    }

    @Test
    fun throwsWhenAuthenticationFails() {
        val useCase = LoginUserUseCase(FakeAuthManager(true), FakeUserGateway(null))
        assertThrows(AuthenticationException::class.java) {
            useCase.execute(User(email = "user@x.com", passwordHash = "bad"))
        }
    }
}

