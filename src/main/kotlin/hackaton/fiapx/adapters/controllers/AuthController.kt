package hackaton.fiapx.adapters.controllers

import hackaton.fiapx.commons.config.jwt.JwtService
import hackaton.fiapx.commons.dto.request.AuthUserRequestV1
import hackaton.fiapx.commons.dto.request.RegisterUserRequestV1
import hackaton.fiapx.commons.dto.response.AuthUserResponseV1
import hackaton.fiapx.commons.exception.UserAlreadyExistsException
import hackaton.fiapx.usecases.auth.LoginUserUseCase
import hackaton.fiapx.usecases.auth.RegisterUserUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val jwtService: JwtService
) {
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterUserRequestV1): ResponseEntity<String> {
        return try {
            registerUserUseCase.execute(request)
            ResponseEntity.ok("Usuário registrado com sucesso")
        } catch (e: UserAlreadyExistsException) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody request: AuthUserRequestV1): ResponseEntity<*> {
        return try {
            val user = loginUserUseCase.execute(request)

            val userDetails = User.builder()
                .username(user?.email)
                .password(user?.passwordHash ?: "")
                .roles("USER")
                .build()

            val token = jwtService.generateToken(userDetails)
            ResponseEntity.ok(AuthUserResponseV1(token))
        } catch (e: AuthenticationException) {
            ResponseEntity.status(401).body("Credenciais inválidas")
        }
    }
}