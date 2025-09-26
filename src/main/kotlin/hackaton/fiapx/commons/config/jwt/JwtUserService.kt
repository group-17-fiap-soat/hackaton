package hackaton.fiapx.commons.config.jwt

import hackaton.fiapx.entities.User
import io.jsonwebtoken.Claims
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtUserService(
    private val jwtService: JwtService
) {

    fun getUserFromRequest(request: HttpServletRequest): User? {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null
        }

        val jwt = authHeader.substring(7)
        return getUserFromToken(jwt)
    }

    fun getUserFromToken(token: String): User? {
        return try {
            val claims = extractAllClaims(token)

            val userId = claims["userId"]?.toString()?.let { UUID.fromString(it) }
            val email = claims["email"]?.toString() ?: claims.subject
            val name = claims["name"]?.toString()

            User(
                id = userId,
                name = name,
                email = email
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return jwtService.extractAllClaims(token)
    }
}
