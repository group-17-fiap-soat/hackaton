package hackaton.fiapx.usecases.auth

import hackaton.fiapx.commons.dto.request.RegisterUserRequestV1
import hackaton.fiapx.commons.exception.UserAlreadyExistsException
import hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface
import hackaton.fiapx.entities.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class RegisterUserUseCase(
    private val userGatewayInterface: UserGatewayInterface,
    private val passwordEncoder: PasswordEncoder
) {

    fun execute(request: RegisterUserRequestV1) {
        if (userGatewayInterface.findByEmail(request.email!!) != null) {
            throw UserAlreadyExistsException("Usuário com o e-mail ${request.email} já existe.")
        }

        val user = User(
            name = request.name,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.pass)
        )

        userGatewayInterface.save(user)
    }
}