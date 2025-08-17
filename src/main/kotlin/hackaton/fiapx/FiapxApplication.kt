package hackaton.fiapx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FiapxApplication

fun main(args: Array<String>) {
    runApplication<FiapxApplication>(*args)
}
