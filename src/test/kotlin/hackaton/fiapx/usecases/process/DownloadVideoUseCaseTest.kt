package hackaton.fiapx.usecases.process

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DownloadVideoUseCaseTest {

    private lateinit var useCase: DownloadVideoUseCase

    @BeforeEach
    fun setup() {
        useCase = DownloadVideoUseCase()
    }

    @Test
    fun `returns file when valid filename exists in outputs directory`(@TempDir tempDir: Path) {
        // Create outputs directory in the current working directory
        val outputsDir = File("outputs")
        outputsDir.mkdirs()

        try {
            val videoFile = File(outputsDir, "test_video.mp4")
            videoFile.writeText("fake video content")

            val result = useCase.execute("test_video.mp4")

            assertNotNull(result, "Expected file to be found but got null")
            assertEquals("test_video.mp4", result.name)
        } finally {
            // Cleanup
            File(outputsDir, "test_video.mp4").delete()
            outputsDir.deleteRecursively()
        }
    }

    @Test
    fun `returns null when file does not exist in outputs directory`() {
        val result = useCase.execute("nonexistent.mp4")
        assertNull(result, "Expected null but got a file")
    }

    @Test
    fun `returns null when filename points to directory instead of file`(@TempDir tempDir: Path) {
        val outputsDir = File("outputs")
        outputsDir.mkdirs()

        try {
            val videoDir = File(outputsDir, "video_dir")
            videoDir.mkdirs()

            val result = useCase.execute("video_dir")
            assertNull(result, "Expected null for directory but got a file")
        } finally {
            // Cleanup
            File(outputsDir, "video_dir").deleteRecursively()
            outputsDir.deleteRecursively()
        }
    }

    @Test
    fun `throws exception when filename contains path traversal attack`() {
        val exception = assertThrows<IllegalArgumentException> {
            useCase.execute("../../../etc/passwd")
        }

        assertEquals("Nome de arquivo inválido", exception.message)
    }

    @Test
    fun `throws exception when filename contains relative path elements`() {
        val exception = assertThrows<IllegalArgumentException> {
            useCase.execute("folder/../secret.txt")
        }

        assertEquals("Nome de arquivo inválido", exception.message)
    }

    @Test
    fun `returns file with different extensions`() {
        val outputsDir = File("outputs")
        outputsDir.mkdirs()

        try {
            val zipFile = File(outputsDir, "frames.zip")
            zipFile.writeText("fake zip content")

            val result = useCase.execute("frames.zip")

            assertNotNull(result)
            assertEquals("frames.zip", result.name)
        } finally {
            // Cleanup
            File(outputsDir, "frames.zip").delete()
            outputsDir.deleteRecursively()
        }
    }

    @Test
    fun `handles filename with multiple dots`() {
        val outputsDir = File("outputs")
        outputsDir.mkdirs()

        try {
            val file = File(outputsDir, "video.backup.mp4")
            file.writeText("fake content")

            val result = useCase.execute("video.backup.mp4")

            assertNotNull(result)
            assertEquals("video.backup.mp4", result.name)
        } finally {
            // Cleanup
            File(outputsDir, "video.backup.mp4").delete()
            outputsDir.deleteRecursively()
        }
    }

    @Test
    fun `handles filename with special characters`() {
        val outputsDir = File("outputs")
        outputsDir.mkdirs()

        try {
            val file = File(outputsDir, "video_123.mp4")
            file.writeText("fake content")

            val result = useCase.execute("video_123.mp4")

            assertNotNull(result)
            assertEquals("video_123.mp4", result.name)
        } finally {
            // Cleanup
            File(outputsDir, "video_123.mp4").delete()
            outputsDir.deleteRecursively()
        }
    }

    @Test
    fun `handles empty filename gracefully`() {
        val result = useCase.execute("")
        assertNull(result)
    }
}
