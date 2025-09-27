package hackaton.fiapx.commons.dto.request

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AuthUserRequestV1Test {

    @Test
    fun `creates auth request with valid credentials`() {
        val request = AuthUserRequestV1(
            email = "user@example.com",
            pass = "password123"
        )

        assertEquals("user@example.com", request.email)
        assertEquals("password123", request.pass)
    }

    @Test
    fun `creates auth request with null values`() {
        val request = AuthUserRequestV1(
            email = null,
            pass = null
        )

        assertEquals(null, request.email)
        assertEquals(null, request.pass)
    }

    @Test
    fun `creates auth request with empty strings`() {
        val request = AuthUserRequestV1(
            email = "",
            pass = ""
        )

        assertEquals("", request.email)
        assertEquals("", request.pass)
    }

    @Test
    fun `maintains data class equality and hashCode`() {
        val request1 = AuthUserRequestV1("user@example.com", "password")
        val request2 = AuthUserRequestV1("user@example.com", "password")
        val request3 = AuthUserRequestV1("different@example.com", "password")

        assertEquals(request1, request2)
        assertEquals(request1.hashCode(), request2.hashCode())
        assertFalse(request1 == request3)
    }

    @Test
    fun `supports property modification`() {
        val request = AuthUserRequestV1()

        request.email = "modified@example.com"
        request.pass = "modifiedpassword"

        assertEquals("modified@example.com", request.email)
        assertEquals("modifiedpassword", request.pass)
    }

    @Test
    fun `handles various password formats`() {
        val passwords = listOf(
            "simple",
            "Complex123!",
            "very_long_password_with_underscores_and_numbers_12345",
            "símbolos-especiais-ção",
            "123456789"
        )

        passwords.forEach { password ->
            val request = AuthUserRequestV1("user@example.com", password)
            assertEquals(password, request.pass)
        }
    }

    @Test
    fun `component functions work correctly`() {
        val request = AuthUserRequestV1("component@example.com", "componentpass")

        val (email, pass) = request

        assertEquals("component@example.com", email)
        assertEquals("componentpass", pass)
    }

    @Test
    fun `handles different email formats`() {
        val emailFormats = listOf(
            "user@domain.com",
            "user.name@domain.co.uk",
            "user+tag@domain.org",
            "123@domain.com"
        )

        emailFormats.forEach { email ->
            val request = AuthUserRequestV1(email = email, pass = "password")
            assertEquals(email, request.email)
        }
    }

    @Test
    fun `handles special characters in fields`() {
        val request = AuthUserRequestV1(
            email = "special+chars@domain-name.co.uk",
            pass = "password!@#$%^&*()"
        )

        assertEquals("special+chars@domain-name.co.uk", request.email)
        assertEquals("password!@#$%^&*()", request.pass)
    }
}
