package hackaton.fiapx.usecases.user

import hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface
import hackaton.fiapx.entities.User
import org.springframework.stereotype.Service

@Service
class GetUserByEmailUseCase(
    private val userGatewayInterface: UserGatewayInterface
) {

    fun execute(email: String): User? {
        return userGatewayInterface.findByEmail(email)
    }
}