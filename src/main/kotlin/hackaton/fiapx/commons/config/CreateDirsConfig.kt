package hackaton.fiapx.commons.config

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.io.File

@Component
class CreateDirsConfig : CommandLineRunner {

    override fun run(vararg args: String?) {
        val dirs = arrayOf("uploads", "outputs", "temp")

        println("Iniciando a criação de diretórios...")

        for (dirName in dirs) {
            val dir = File(dirName)
            if (!dir.exists()) {
                val created = dir.mkdirs()
                if (created) {
                    println("Diretório '$dirName' criado com sucesso.")
                } else {
                    println("Falha ao criar o diretório '$dirName'.")
                }
            } else {
                println("Diretório '$dirName' já existe.")
            }
        }
    }
}