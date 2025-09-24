package hackaton.fiapx.usecases.auth

import hackaton.fiapx.commons.dto.request.AuthUserRequestV1
import hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface
import hackaton.fiapx.entities.User
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import javax.naming.AuthenticationException

@Service
class LoginUserUseCase(
    private val authenticationManager: AuthenticationManager,
    private val userGatewayInterface: UserGatewayInterface
) {
    @Throws(AuthenticationException::class)
    fun execute(user: User): User? {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(user.email, user.passwordHash)
        )

        return userGatewayInterface.findByEmail(user.email!!)
    }
}