package hackaton.fiapx.commons.config.jwt

import hackaton.fiapx.entities.User
import io.jsonwebtoken.Claims
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.mock.web.MockHttpServletRequest
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class JwtUserServiceTest {

    private lateinit var jwtService: JwtService
    private lateinit var jwtUserService: JwtUserService

    @BeforeEach
    fun setup() {
        jwtService = mock()
        jwtUserService = JwtUserService(jwtService)
    }

    @Test
    fun `extracts user from valid JWT token in request header`() {
        val request = MockHttpServletRequest()
        val token = "valid.jwt.token"
        val userId = UUID.randomUUID()
        val claims = mock<Claims>()

        whenever(claims.get("userId")).thenReturn(userId.toString())
        whenever(claims.get("email")).thenReturn("user@example.com")
        whenever(claims.get("name")).thenReturn("Test User")
        whenever(claims.subject).thenReturn("user@example.com")

        request.addHeader("Authorization", "Bearer $token")
        whenever(jwtService.extractAllClaims(token)).thenReturn(claims)

        val user = jwtUserService.getUserFromRequest(request)

        assertNotNull(user)
        assertEquals(userId, user.id)
        assertEquals("user@example.com", user.email)
        assertEquals("Test User", user.name)
    }

    @Test
    fun `returns null when authorization header is missing`() {
        val request = MockHttpServletRequest()

        val user = jwtUserService.getUserFromRequest(request)

        assertNull(user)
        verifyNoInteractions(jwtService)
    }

    @Test
    fun `returns null when authorization header does not start with Bearer`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Basic dGVzdDp0ZXN0")

        val user = jwtUserService.getUserFromRequest(request)

        assertNull(user)
        verifyNoInteractions(jwtService)
    }

    @Test
    fun `returns null when JWT token is malformed`() {
        val request = MockHttpServletRequest()
        val malformedToken = "malformed.token"

        request.addHeader("Authorization", "Bearer $malformedToken")
        whenever(jwtService.extractAllClaims(malformedToken)).thenThrow(RuntimeException("Invalid token"))

        val user = jwtUserService.getUserFromRequest(request)

        assertNull(user)
    }

    @Test
    fun `extracts user from token directly`() {
        val token = "valid.jwt.token"
        val userId = UUID.randomUUID()
        val claims = mock<Claims>()

        whenever(claims.get("userId")).thenReturn(userId.toString())
        whenever(claims.get("email")).thenReturn("direct@example.com")
        whenever(claims.get("name")).thenReturn("Direct User")

        whenever(jwtService.extractAllClaims(token)).thenReturn(claims)

        val user = jwtUserService.getUserFromToken(token)

        assertNotNull(user)
        assertEquals(userId, user.id)
        assertEquals("direct@example.com", user.email)
        assertEquals("Direct User", user.name)
    }

    @Test
    fun `uses subject as email when email claim is missing`() {
        val request = MockHttpServletRequest()
        val token = "valid.jwt.token"
        val userId = UUID.randomUUID()
        val claims = mock<Claims>()

        whenever(claims.get("userId")).thenReturn(userId.toString())
        whenever(claims.get("email")).thenReturn(null)
        whenever(claims.get("name")).thenReturn("Subject User")
        whenever(claims.subject).thenReturn("subject@example.com")

        request.addHeader("Authorization", "Bearer $token")
        whenever(jwtService.extractAllClaims(token)).thenReturn(claims)

        val user = jwtUserService.getUserFromRequest(request)

        assertNotNull(user)
        assertEquals("subject@example.com", user.email)
        assertEquals("Subject User", user.name)
    }

    @Test
    fun `handles missing userId claim gracefully`() {
        val request = MockHttpServletRequest()
        val token = "valid.jwt.token"
        val claims = mock<Claims>()

        whenever(claims.get("userId")).thenReturn(null)
        whenever(claims.get("email")).thenReturn("nouid@example.com")
        whenever(claims.get("name")).thenReturn("No UID User")

        request.addHeader("Authorization", "Bearer $token")
        whenever(jwtService.extractAllClaims(token)).thenReturn(claims)

        val user = jwtUserService.getUserFromRequest(request)

        assertNotNull(user)
        assertNull(user.id)
        assertEquals("nouid@example.com", user.email)
        assertEquals("No UID User", user.name)
    }

    @Test
    fun `handles missing name claim gracefully`() {
        val request = MockHttpServletRequest()
        val token = "valid.jwt.token"
        val userId = UUID.randomUUID()
        val claims = mock<Claims>()

        whenever(claims.get("userId")).thenReturn(userId.toString())
        whenever(claims.get("email")).thenReturn("noname@example.com")
        whenever(claims.get("name")).thenReturn(null)

        request.addHeader("Authorization", "Bearer $token")
        whenever(jwtService.extractAllClaims(token)).thenReturn(claims)

        val user = jwtUserService.getUserFromRequest(request)

        assertNotNull(user)
        assertEquals(userId, user.id)
        assertEquals("noname@example.com", user.email)
        assertNull(user.name)
    }

    @Test
    fun `handles invalid UUID format in userId claim`() {
        val request = MockHttpServletRequest()
        val token = "valid.jwt.token"
        val claims = mock<Claims>()

        whenever(claims.get("userId")).thenReturn("invalid-uuid-format")
        whenever(claims.get("email")).thenReturn("invalid@example.com")
        whenever(claims.get("name")).thenReturn("Invalid UUID User")

        request.addHeader("Authorization", "Bearer $token")
        whenever(jwtService.extractAllClaims(token)).thenReturn(claims)

        val user = jwtUserService.getUserFromRequest(request)

        assertNull(user)
    }

    @Test
    fun `handles empty authorization header value`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "")

        val user = jwtUserService.getUserFromRequest(request)

        assertNull(user)
        verifyNoInteractions(jwtService)
    }

    @Test
    fun `handles authorization header with only Bearer prefix`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer ")

        val user = jwtUserService.getUserFromRequest(request)

        assertNull(user)
    }

    @Test
    fun `extracts user with minimal claims`() {
        val request = MockHttpServletRequest()
        val token = "minimal.jwt.token"
        val claims = mock<Claims>()

        whenever(claims.get("userId")).thenReturn(null)
        whenever(claims.get("email")).thenReturn(null)
        whenever(claims.get("name")).thenReturn(null)
        whenever(claims.subject).thenReturn("minimal@example.com")

        request.addHeader("Authorization", "Bearer $token")
        whenever(jwtService.extractAllClaims(token)).thenReturn(claims)

        val user = jwtUserService.getUserFromRequest(request)

        assertNotNull(user)
        assertNull(user.id)
        assertEquals("minimal@example.com", user.email)
        assertNull(user.name)
    }

    @Test
    fun `handles case insensitive Bearer prefix`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "bearer valid.jwt.token")

        val user = jwtUserService.getUserFromRequest(request)

        assertNull(user)
        verifyNoInteractions(jwtService)
    }

    @Test
    fun `handles whitespace in authorization header`() {
        val request = MockHttpServletRequest()
        val token = "valid.jwt.token"
        val userId = UUID.randomUUID()
        val claims = mock<Claims>()

        whenever(claims.get("userId")).thenReturn(userId.toString())
        whenever(claims.get("email")).thenReturn("whitespace@example.com")
        whenever(claims.get("name")).thenReturn(null)
        whenever(claims.subject).thenReturn("whitespace@example.com")

        request.addHeader("Authorization", "Bearer   $token   ")
        whenever(jwtService.extractAllClaims("  $token   ")).thenReturn(claims)

        val user = jwtUserService.getUserFromRequest(request)

        assertNotNull(user)
        assertEquals(userId, user.id)
        assertEquals("whitespace@example.com", user.email)
    }
}
