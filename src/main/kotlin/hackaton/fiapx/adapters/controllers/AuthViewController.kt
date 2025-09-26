package hackaton.fiapx.adapters.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AuthViewController {

    @GetMapping("/")
    fun redirectToLogin(): String {
        return "redirect:/login"
    }

    @GetMapping("/login")
    fun loginPage(): String {
        return "login.html"
    }

    @GetMapping("/register")
    fun registerPage(): String {
        return "register.html"
    }

    @GetMapping("/home")
    fun homePage(): String {
        return "home.html"
    }
}