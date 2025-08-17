package hackaton.fiapx.usecases

import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class CreateZipFileUseCase {

    fun execute(files: List<File>, zipPath: File): Boolean {
        try {
            ZipOutputStream(zipPath.outputStream()).use { zipOut ->
                for (file in files) {
                    if (!addFileToZip(zipOut, file)) {
                        return false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun addFileToZip(zipOut: ZipOutputStream, file: File): Boolean {
        return try {
            FileInputStream(file).use { input ->
                val zipEntry = ZipEntry(file.name)
                zipOut.putNextEntry(zipEntry)
                input.copyTo(zipOut)
                zipOut.closeEntry()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}