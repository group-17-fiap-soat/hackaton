package hackaton.fiapx.usecases

import hackaton.fiapx.commons.dto.response.VideoProcessResponseV1
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Paths

@Service
class ProcessVideoUseCase(
    private val createZipFile: CreateZipFileUseCase
) {

    fun execute(videoPath: String, timestamp: String): VideoProcessResponseV1 {
        println("Iniciando processamento: $videoPath")

        val tempDir = File("temp", timestamp)
        tempDir.mkdirs()

        try {
            val framePattern = "${tempDir.absolutePath}/frame_%04d.png"

            val processBuilder = ProcessBuilder(
                "ffmpeg",
                "-i", videoPath,
                "-vf", "fps=1",
                "-y",
                framePattern
            ).redirectErrorStream(true)

            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode != 0) {
                println("Erro no  ffmpeg: Exit $exitCode\nOutput: $output")
                return VideoProcessResponseV1(status = VideoProcessStatusEnum.ERROR)
            }

            val frames = tempDir.listFiles { _, name -> name.endsWith(".png") }?.toList() ?: emptyList()

            if (frames.isEmpty()) {
                println("Nenhum frame foi extraído do vídeo")
                return VideoProcessResponseV1(status = VideoProcessStatusEnum.ERROR)
            }

            println("📸 Extraídos ${frames.size} frames")

            val zipFilename = "frames_${timestamp}.zip"
            val zipPath = Paths.get("outputs", zipFilename).toFile()

            try {
                createZipFile.execute(frames, zipPath)
            } catch (e: IOException) {
                println("Erro ao criar arquivo ZIP: ${e.message}")
                return VideoProcessResponseV1(status = VideoProcessStatusEnum.ERROR)
            }

            println("✅ ZIP criado: ${zipPath.absolutePath}")

            return VideoProcessResponseV1(
                zipPath = zipFilename,
                frameCount = frames.size,
                status = VideoProcessStatusEnum.SUCCESS
            )

        } finally {
            tempDir.deleteRecursively()
        }
    }
}