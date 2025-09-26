package hackaton.fiapx.adapters.controllers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuthViewControllerTest {

    @Test
    fun redirectsRootToLogin() {
        val controller = AuthViewController()

        val result = controller.redirectToLogin()

        assertEquals("redirect:/login", result)
    }

    @Test
    fun returnsLoginPageTemplate() {
        val controller = AuthViewController()

        val result = controller.loginPage()

        assertEquals("login.html", result)
    }

    @Test
    fun returnsRegisterPageTemplate() {
        val controller = AuthViewController()

        val result = controller.registerPage()

        assertEquals("register.html", result)
    }

    @Test
    fun returnsHomePageTemplate() {
        val controller = AuthViewController()

        val result = controller.homePage()

        assertEquals("home.html", result)
    }
}
