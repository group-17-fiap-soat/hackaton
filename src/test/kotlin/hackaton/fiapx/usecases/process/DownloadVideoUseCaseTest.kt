package hackaton.fiapx.usecases.process

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DownloadVideoUseCaseTest {

    @BeforeEach
    fun setup() {
        File("outputs").mkdirs()
    }

    @AfterEach
    fun cleanup() {
        File("outputs").deleteRecursively()
    }

    @Test
    fun returnsFileWhenItExists() {
        val f = File("outputs", "file.zip").apply { writeText("x") }
        val result = DownloadVideoUseCase().execute("file.zip")
        org.junit.jupiter.api.Assertions.assertNotNull(result)
        assertEquals(f.absolutePath, result!!.absolutePath)
    }

    @Test
    fun returnsNullWhenFileDoesNotExist() {
        val result = DownloadVideoUseCase().execute("missing.zip")
        org.junit.jupiter.api.Assertions.assertNull(result)
    }

    @Test
    fun throwsWhenFilenameIsInvalid() {
        assertThrows(IllegalArgumentException::class.java) {
            DownloadVideoUseCase().execute("../evil.zip")
        }
    }
}
