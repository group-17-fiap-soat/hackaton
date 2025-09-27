package hackaton.fiapx.commons.dto.request

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RegisterUserRequestV1Test {

    @Test
    fun `creates registration request with all valid fields`() {
        val request = RegisterUserRequestV1(
            name = "John Doe",
            email = "john@example.com",
            pass = "securepassword"
        )

        assertEquals("John Doe", request.name)
        assertEquals("john@example.com", request.email)
        assertEquals("securepassword", request.pass)
    }

    @Test
    fun `creates registration request with null values`() {
        val request = RegisterUserRequestV1(
            name = null,
            email = null,
            pass = null
        )

        assertEquals(null, request.name)
        assertEquals(null, request.email)
        assertEquals(null, request.pass)
    }

    @Test
    fun `creates registration request with empty strings`() {
        val request = RegisterUserRequestV1(
            name = "",
            email = "",
            pass = ""
        )

        assertEquals("", request.name)
        assertEquals("", request.email)
        assertEquals("", request.pass)
    }

    @Test
    fun `handles various name formats`() {
        val names = listOf(
            "John Doe",
            "María García",
            "李小明",
            "Jean-Pierre Dupont",
            "O'Connor",
            "van der Berg",
            "Smith Jr.",
            "José María de la Cruz"
        )

        names.forEach { name ->
            val request = RegisterUserRequestV1(
                name = name,
                email = "user@example.com",
                pass = "password"
            )
            assertEquals(name, request.name)
        }
    }

    @Test
    fun `maintains data class equality and hashCode`() {
        val request1 = RegisterUserRequestV1("John", "john@example.com", "pass")
        val request2 = RegisterUserRequestV1("John", "john@example.com", "pass")
        val request3 = RegisterUserRequestV1("Jane", "john@example.com", "pass")

        assertEquals(request1, request2)
        assertEquals(request1.hashCode(), request2.hashCode())
        assertFalse(request1 == request3)
    }

    @Test
    fun `supports property modification`() {
        val request = RegisterUserRequestV1()

        request.name = "Modified Name"
        request.email = "modified@example.com"
        request.pass = "modifiedpassword"

        assertEquals("Modified Name", request.name)
        assertEquals("modified@example.com", request.email)
        assertEquals("modifiedpassword", request.pass)
    }

    @Test
    fun `handles long names and passwords`() {
        val longName = "A".repeat(100)
        val longPassword = "P".repeat(200)

        val request = RegisterUserRequestV1(
            name = longName,
            email = "long@example.com",
            pass = longPassword
        )

        assertEquals(longName, request.name)
        assertEquals("long@example.com", request.email)
        assertEquals(longPassword, request.pass)
    }

    @Test
    fun `component functions work correctly`() {
        val request = RegisterUserRequestV1(
            "Component User",
            "component@example.com",
            "componentpass"
        )

        val (name, email, pass) = request

        assertEquals("Component User", name)
        assertEquals("component@example.com", email)
        assertEquals("componentpass", pass)
    }

    @Test
    fun `handles special characters in passwords`() {
        val specialPasswords = listOf(
            "password!@#$%^&*()",
            "pássword-with-açcents",
            "пароль123",
            "密码password",
            "pass word with spaces"
        )

        specialPasswords.forEach { password ->
            val request = RegisterUserRequestV1(
                name = "Test User",
                email = "user@example.com",
                pass = password
            )
            assertEquals(password, request.pass)
        }
    }

    @Test
    fun `handles different email formats`() {
        val emailFormats = listOf(
            "user@domain.com",
            "user.name@domain.co.uk",
            "user+tag@domain.org",
            "123user@domain.com",
            "user_name@subdomain.domain.com"
        )

        emailFormats.forEach { email ->
            val request = RegisterUserRequestV1(
                name = "Test User",
                email = email,
                pass = "password"
            )
            assertEquals(email, request.email)
        }
    }
}
