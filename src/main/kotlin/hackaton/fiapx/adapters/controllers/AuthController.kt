package hackaton.fiapx.adapters.controllers

import hackaton.fiapx.adapters.presenters.UserMapper
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
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import hackaton.fiapx.adapters.controllers.operation.AuthOperation

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder

) : AuthOperation {
    @PostMapping("/register")
    override fun register(@RequestBody request: RegisterUserRequestV1): ResponseEntity<String> {
        return try {
            registerUserUseCase.execute(
                UserMapper.fromRequestToDomain(
                    request.copy(passwordEncoder.encode(request.pass))
                )
            )
            ResponseEntity.ok("Usuário registrado com sucesso")
        } catch (e: UserAlreadyExistsException) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @PostMapping("/login")
    override fun login(@RequestBody request: AuthUserRequestV1): ResponseEntity<Any> {
        return try {
            val user = loginUserUseCase.execute(UserMapper.fromRequestToDomain(request))

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