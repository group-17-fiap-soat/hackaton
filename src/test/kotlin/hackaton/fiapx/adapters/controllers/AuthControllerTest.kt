package hackaton.fiapx.adapters.controllers

import hackaton.fiapx.commons.dto.request.AuthUserRequestV1
import hackaton.fiapx.commons.dto.request.RegisterUserRequestV1
import hackaton.fiapx.commons.dto.response.AuthUserResponseV1
import hackaton.fiapx.commons.exception.UserAlreadyExistsException
import hackaton.fiapx.entities.User
import hackaton.fiapx.usecases.auth.LoginUserUseCase
import hackaton.fiapx.usecases.auth.RegisterUserUseCase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class AuthControllerTest {

    private class FakeRegisterUserUseCase(private val throwException: Boolean = false) : RegisterUserUseCase(object : hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface {
        override fun findById(id: UUID) = null
        override fun findByEmail(email: String) = null
        override fun save(entity: User) = entity
    }) {
        override fun execute(user: User) {
            if (throwException) throw UserAlreadyExistsException("Usuário já existe")
        }
    }

    private class FakeLoginUserUseCase(private val returnUser: User?, private val throwException: Boolean = false) : LoginUserUseCase(
        object : org.springframework.security.authentication.AuthenticationManager {
            override fun authenticate(authentication: org.springframework.security.core.Authentication): org.springframework.security.core.Authentication {
                if (throwException) throw org.springframework.security.authentication.BadCredentialsException("Invalid credentials")
                return authentication
            }
        },
        object : hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface {
            override fun findById(id: UUID) = null
            override fun findByEmail(email: String) = returnUser
            override fun save(entity: User) = entity
        }
    ) {
        override fun execute(user: User): User? {
            if (throwException) throw org.springframework.security.authentication.BadCredentialsException("Invalid credentials")
            return returnUser
        }
    }

    private class FakeJwtService : hackaton.fiapx.commons.config.jwt.JwtService("fake-secret", 86400000L) {
        override fun generateToken(userDetails: org.springframework.security.core.userdetails.UserDetails): String {
            return "fake-jwt-token"
        }
        override fun extractUsername(token: String): String = "user@test.com"
        override fun isTokenValid(token: String, userDetails: org.springframework.security.core.userdetails.UserDetails): Boolean = true
    }

    private class FakePasswordEncoder : PasswordEncoder {
        override fun encode(rawPassword: CharSequence): String = "encoded-$rawPassword"
        override fun matches(rawPassword: CharSequence, encodedPassword: String): Boolean = true
    }

    @Test
    fun registersUserSuccessfully() {
        val controller = AuthController(
            FakeRegisterUserUseCase(),
            FakeLoginUserUseCase(null),
            FakeJwtService(),
            FakePasswordEncoder()
        )
        val request = RegisterUserRequestV1("John Doe", "john@test.com", "password123")

        val response = controller.register(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Usuário registrado com sucesso", response.body)
    }

    @Test
    fun returnsBadRequestWhenUserAlreadyExists() {
        val controller = AuthController(
            FakeRegisterUserUseCase(true),
            FakeLoginUserUseCase(null),
            FakeJwtService(),
            FakePasswordEncoder()
        )
        val request = RegisterUserRequestV1("John Doe", "existing@test.com", "password123")

        val response = controller.register(request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Usuário já existe", response.body)
    }

    @Test
    fun loginReturnsJwtTokenWhenCredentialsAreValid() {
        val user = User(UUID.randomUUID(), "John", "john@test.com", "hashedPassword")
        val controller = AuthController(
            FakeRegisterUserUseCase(),
            FakeLoginUserUseCase(user),
            FakeJwtService(),
            FakePasswordEncoder()
        )
        val request = AuthUserRequestV1("john@test.com", "password123")

        val response = controller.login(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body is AuthUserResponseV1)
        val authResponse = response.body as AuthUserResponseV1
        assertEquals("fake-jwt-token", authResponse.accessToken)
    }
}
