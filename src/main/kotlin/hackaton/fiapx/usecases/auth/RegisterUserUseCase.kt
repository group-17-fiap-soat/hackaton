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
) {

    fun execute(user: User) {
        if (userGatewayInterface.findByEmail(user.email!!) != null) {
            throw UserAlreadyExistsException("Usuário com o e-mail ${user.email} já existe.")
        }

        userGatewayInterface.save(user)
    }
}