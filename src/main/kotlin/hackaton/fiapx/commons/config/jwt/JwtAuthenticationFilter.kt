package hackaton.fiapx.commons.config.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Skip JWT authentication for Swagger endpoints
        val requestPath = request.requestURI
        val swaggerPaths = listOf(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars"
        )

        if (swaggerPaths.any { requestPath.startsWith(it) }) {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7)
        val userEmail = jwtService.extractUsername(jwt)

        if (SecurityContextHolder.getContext().authentication == null) {
            // Criar UserDetails diretamente sem buscar no banco
            val userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(userEmail)
                .password("") // Não precisamos da senha pois usamos JWT
                .authorities("USER")
                .build()

            if (jwtService.isTokenValid(jwt, userDetails)) {
                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }
}