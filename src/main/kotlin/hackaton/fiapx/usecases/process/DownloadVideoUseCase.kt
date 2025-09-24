package hackaton.fiapx.usecases.process

import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

@Service
class DownloadVideoUseCase {

    fun execute(filename: String): File? {
        if (filename.contains("..")) {
            throw IllegalArgumentException("Nome de arquivo inválido")
        }

        val filePath: Path = Paths.get("outputs", filename).toAbsolutePath().normalize()
        val file = filePath.toFile()

        return if (file.exists() && file.isFile) file else null
    }
}