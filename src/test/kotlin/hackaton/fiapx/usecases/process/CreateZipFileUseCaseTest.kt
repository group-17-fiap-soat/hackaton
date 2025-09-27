package hackaton.fiapx.usecases.process

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CreateZipFileUseCaseTest {

    private val useCase = CreateZipFileUseCase()

    @Test
    fun `creates zip file with single file successfully`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("Test content")
        val zipFile = File(tempDir, "output.zip")

        val result = useCase.execute(listOf(testFile), zipFile)

        assertTrue(result)
        assertTrue(zipFile.exists())

        ZipFile(zipFile).use { zip ->
            assertEquals(1, zip.size())
            val entry = zip.getEntry("test.txt")
            assertEquals("test.txt", entry.name)
        }
    }

    @Test
    fun `creates zip file with multiple files successfully`(@TempDir tempDir: File) {
        val file1 = File(tempDir, "file1.txt")
        val file2 = File(tempDir, "file2.txt")
        file1.writeText("Content 1")
        file2.writeText("Content 2")
        val zipFile = File(tempDir, "multiple.zip")

        val result = useCase.execute(listOf(file1, file2), zipFile)

        assertTrue(result)
        assertTrue(zipFile.exists())

        ZipFile(zipFile).use { zip ->
            assertEquals(2, zip.size())
            assertTrue(zip.getEntry("file1.txt") != null)
            assertTrue(zip.getEntry("file2.txt") != null)
        }
    }

    @Test
    fun `creates empty zip file when no files provided`(@TempDir tempDir: File) {
        val zipFile = File(tempDir, "empty.zip")

        val result = useCase.execute(emptyList(), zipFile)

        assertTrue(result)
        assertTrue(zipFile.exists())

        ZipFile(zipFile).use { zip ->
            assertEquals(0, zip.size())
        }
    }

    @Test
    fun `preserves file content in zip entries`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "content.txt")
        val expectedContent = "This is test content with special chars: áéíóú"
        testFile.writeText(expectedContent)
        val zipFile = File(tempDir, "content.zip")

        val result = useCase.execute(listOf(testFile), zipFile)

        assertTrue(result)

        ZipFile(zipFile).use { zip ->
            val entry = zip.getEntry("content.txt")
            val actualContent = zip.getInputStream(entry).readAllBytes().toString(Charsets.UTF_8)
            assertEquals(expectedContent, actualContent)
        }
    }

    @Test
    fun `returns false when source file does not exist`(@TempDir tempDir: File) {
        val nonExistentFile = File(tempDir, "nonexistent.txt")
        val zipFile = File(tempDir, "output.zip")

        val result = useCase.execute(listOf(nonExistentFile), zipFile)

        assertFalse(result)
        // Implementation throws FileNotFoundException, caught by try-catch and returns false
    }

    @Test
    fun `returns false when zip file directory does not exist`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("content")
        val invalidZipPath = File(File(tempDir, "nonexistent"), "output.zip")

        val result = useCase.execute(listOf(testFile), invalidZipPath)

        assertFalse(result)
        // Implementation throws FileNotFoundException when trying to create output stream
    }

    @Test
    fun `handles files with same name correctly - returns false on duplicate`(@TempDir tempDir: File) {
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()

        val file1 = File(tempDir, "same.txt")
        val file2 = File(subDir, "same.txt")
        file1.writeText("Content 1")
        file2.writeText("Content 2")
        val zipFile = File(tempDir, "samename.zip")

        val result = useCase.execute(listOf(file1, file2), zipFile)

        // The implementation throws ZipException for duplicate entries, caught and returns false
        assertFalse(result)
    }

    @Test
    fun `handles large file successfully`(@TempDir tempDir: File) {
        val largeFile = File(tempDir, "large.txt")
        val largeContent = "x".repeat(10000)
        largeFile.writeText(largeContent)
        val zipFile = File(tempDir, "large.zip")

        val result = useCase.execute(listOf(largeFile), zipFile)

        assertTrue(result)
        assertTrue(zipFile.exists())
        assertTrue(zipFile.length() > 0)
        // ZIP compression should make it smaller than the original
        assertTrue(zipFile.length() < largeFile.length())
    }

    @Test
    fun `handles empty file successfully`(@TempDir tempDir: File) {
        val emptyFile = File(tempDir, "empty.txt")
        emptyFile.createNewFile()
        val zipFile = File(tempDir, "withempty.zip")

        val result = useCase.execute(listOf(emptyFile), zipFile)

        assertTrue(result)

        ZipFile(zipFile).use { zip ->
            assertEquals(1, zip.size())
            val entry = zip.getEntry("empty.txt")
            assertEquals(0, entry.size)
        }
    }

    @Test
    fun `overwrites existing zip file`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("New content")
        val zipFile = File(tempDir, "existing.zip")

        // Create an existing file (not a valid ZIP)
        zipFile.writeText("Old zip content")
        val originalSize = zipFile.length()

        val result = useCase.execute(listOf(testFile), zipFile)

        assertTrue(result)
        assertTrue(zipFile.length() != originalSize)

        ZipFile(zipFile).use { zip ->
            assertEquals(1, zip.size())
            assertEquals("test.txt", zip.entries().nextElement().name)
        }
    }

    @Test
    fun `handles mixed existing and non-existing files`(@TempDir tempDir: File) {
        val existingFile = File(tempDir, "exists.txt")
        existingFile.writeText("I exist")
        val nonExistentFile = File(tempDir, "missing.txt")
        val zipFile = File(tempDir, "mixed.zip")

        val result = useCase.execute(listOf(existingFile, nonExistentFile), zipFile)

        // Should return false because one file doesn't exist (FileNotFoundException)
        assertFalse(result)
    }
}
