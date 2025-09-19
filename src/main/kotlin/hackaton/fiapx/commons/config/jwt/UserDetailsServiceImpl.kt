package hackaton.fiapx.commons.config.jwt

import hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(private val userGatewayInterface: UserGatewayInterface) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val userDAO = userGatewayInterface.findByEmail(username)
            ?: throw UsernameNotFoundException("Usuário não encontrado com o email: $username")

        return User.builder()
            .username(userDAO.email)
            .password(userDAO.passwordHash)
            .roles("USER")
            .build()
    }
}