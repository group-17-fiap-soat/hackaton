package hackaton.fiapx.usecases.process

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class CreateZipFileUseCaseTest {

    @Test
    fun returnsTrueAndCreatesZipWhenAllFilesValid() {
        val tmpDir = createTempDir(prefix = "zip-ok-")
        val f1 = File(tmpDir, "a.txt").apply { writeText("A") }
        val f2 = File(tmpDir, "b.txt").apply { writeText("B") }
        val zip = File(tmpDir, "out.zip")

        val result = CreateZipFileUseCase().execute(listOf(f1, f2), zip)

        assertTrue(result)
        assertTrue(zip.exists())
        tmpDir.deleteRecursively()
    }

    @Test
    fun returnsFalseWhenAFileCannotBeAdded() {
        val tmpDir = createTempDir(prefix = "zip-bad-file-")
        val bad = File(tmpDir, "dir").apply { mkdirs() }
        val zip = File(tmpDir, "out.zip")

        val result = CreateZipFileUseCase().execute(listOf(bad), zip)

        assertFalse(result)
        tmpDir.deleteRecursively()
    }

    @Test
    fun returnsFalseWhenZipCannotBeCreated() {
        val tmpDir = createTempDir(prefix = "zip-bad-target-")
        val f1 = File(tmpDir, "a.txt").apply { writeText("A") }
        val zipDirAsFile = File(tmpDir, "cant-create").apply { mkdirs() }

        val result = CreateZipFileUseCase().execute(listOf(f1), zipDirAsFile)

        assertFalse(result)
        tmpDir.deleteRecursively()
    }
}
