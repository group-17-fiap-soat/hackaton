package hackaton.fiapx.commons.config.jwt

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        // Como estamos usando JWT, este método será chamado apenas para validação do token
        // As informações do usuário já estão no JWT, então retornamos um usuário básico
        return User.builder()
            .username(username)
            .password("") // Não precisamos da senha pois usamos JWT
            .authorities("USER")
            .build()
    }
}