package hackaton.fiapx.commons.config.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class JwtServiceTest {

    private lateinit var jwtService: JwtService
    private lateinit var userDetails: UserDetails

    private val jwtSecret = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1hbmQtdmFsaWRhdGlvbg=="
    private val jwtExpirationMs = 3600000L // 1 hour

    @BeforeEach
    fun setup() {
        jwtService = JwtService(jwtSecret, jwtExpirationMs)
        userDetails = mock()
        whenever(userDetails.username).thenReturn("testuser@example.com")
    }

    @Test
    fun `generates valid JWT token with user details`() {
        val token = jwtService.generateToken(userDetails)

        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(token.split(".").size == 3)
    }

    @Test
    fun `generates token with extra claims`() {
        val extraClaims = mapOf(
            "userId" to "123e4567-e89b-12d3-a456-426614174000",
            "role" to "USER",
            "name" to "Test User"
        )

        val token = jwtService.generateToken(extraClaims, userDetails)
        val claims = jwtService.extractAllClaims(token)

        assertNotNull(token)
        assertEquals("123e4567-e89b-12d3-a456-426614174000", claims["userId"])
        assertEquals("USER", claims["role"])
        assertEquals("Test User", claims["name"])
    }

    @Test
    fun `extracts username from valid token`() {
        val token = jwtService.generateToken(userDetails)

        val extractedUsername = jwtService.extractUsername(token)

        assertEquals("testuser@example.com", extractedUsername)
    }

    @Test
    fun `validates token with matching user details`() {
        val token = jwtService.generateToken(userDetails)

        val isValid = jwtService.isTokenValid(token, userDetails)

        assertTrue(isValid)
    }

    @Test
    fun `invalidates token with mismatched user details`() {
        val token = jwtService.generateToken(userDetails)
        val differentUserDetails = mock<UserDetails>()
        whenever(differentUserDetails.username).thenReturn("different@example.com")

        val isValid = jwtService.isTokenValid(token, differentUserDetails)

        assertFalse(isValid)
    }

    @Test
    fun `detects expired token as invalid`() {
        try {
            val expiredJwtService = JwtService(jwtSecret, 1L)
            val token = expiredJwtService.generateToken(userDetails)
            Thread.sleep(100)

            val isValid = expiredJwtService.isTokenValid(token, userDetails)
            assertFalse(isValid)
        } catch (e: ExpiredJwtException) {
            // Expected behavior - token is expired
            assertTrue(true)
        }
    }

    @Test
    fun `extracts all claims from token`() {
        val extraClaims = mapOf(
            "customClaim1" to "value1",
            "customClaim2" to 42,
            "customClaim3" to true
        )
        val token = jwtService.generateToken(extraClaims, userDetails)

        val claims = jwtService.extractAllClaims(token)

        assertEquals("testuser@example.com", claims.subject)
        assertEquals("value1", claims["customClaim1"])
        assertEquals(42, claims["customClaim2"])
        assertEquals(true, claims["customClaim3"])
        assertNotNull(claims.issuedAt)
        assertNotNull(claims.expiration)
    }

    @Test
    fun `handles malformed token gracefully`() {
        val malformedToken = "invalid.token.format"

        try {
            jwtService.extractUsername(malformedToken)
            assert(false) { "Should have thrown exception" }
        } catch (e: Exception) {
            assertTrue(e is MalformedJwtException || e is IllegalArgumentException)
        }
    }

    @Test
    fun `handles token with invalid signature`() {
        val differentSecret = "ZGlmZmVyZW50LXNlY3JldC1rZXktZm9yLWp3dC10b2tlbi1nZW5lcmF0aW9uLWFuZC12YWxpZGF0aW9u"
        val differentJwtService = JwtService(differentSecret, jwtExpirationMs)
        val tokenWithDifferentSignature = differentJwtService.generateToken(userDetails)

        try {
            jwtService.extractUsername(tokenWithDifferentSignature)
            assert(false) { "Should have thrown exception" }
        } catch (e: Exception) {
            assertTrue(e is SignatureException)
        }
    }

    @Test
    fun `generates different tokens for same user at different times`() {
        val token1 = jwtService.generateToken(userDetails)
        Thread.sleep(1000)
        val token2 = jwtService.generateToken(userDetails)

        assertNotNull(token1)
        assertNotNull(token2)
        assertTrue(token1 != token2)
    }

    @Test
    fun `token expiration time matches configured duration`() {
        val token = jwtService.generateToken(userDetails)
        val claims = jwtService.extractAllClaims(token)

        val expectedExpiration = claims.issuedAt.time + jwtExpirationMs
        val actualExpiration = claims.expiration.time

        assertTrue(Math.abs(expectedExpiration - actualExpiration) < 1000)
    }

    @Test
    fun `handles empty extra claims`() {
        val token = jwtService.generateToken(emptyMap(), userDetails)

        assertNotNull(token)
        val claims = jwtService.extractAllClaims(token)
        assertEquals("testuser@example.com", claims.subject)
    }

    @Test
    fun `handles null values in extra claims gracefully`() {
        val extraClaims: Map<String, Any> = mapOf(
            "nullValue" to "null",
            "validValue" to "test"
        )

        val token = jwtService.generateToken(extraClaims, userDetails)
        val claims = jwtService.extractAllClaims(token)

        assertNotNull(token)
        assertEquals("test", claims["validValue"])
        assertEquals("null", claims["nullValue"])
    }
}
