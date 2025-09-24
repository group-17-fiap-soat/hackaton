package hackaton.fiapx.usecases

import hackaton.fiapx.commons.dto.response.VideoResponseV1
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.entities.Video
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Paths

@Service
class ProcessVideoUseCase(
    private val createZipFile: CreateZipFileUseCase,
) {

    fun execute(video : Video): VideoResponseV1 {
        println("Iniciando processamento: $video.originalVideoPath")

        val tempDir = File("temp", video.uploadedAt.toString())
        tempDir.mkdirs()

        try {
            val framePattern = "${tempDir.absolutePath}/frame_%04d.png"

            val processBuilder = ProcessBuilder(
                "ffmpeg",
                "-i", video.originalVideoPath!!.split("\\").last(),
                "-vf", "fps=1",
                "-y",
                framePattern
            ).redirectErrorStream(true)

            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode != 0) {
                println("Erro no  ffmpeg: Exit $exitCode\nOutput: $output")
            }

            val frames = tempDir.listFiles { _, name -> name.endsWith(".png") }?.toList() ?: emptyList()

            if (frames.isEmpty()) {
                println("Nenhum frame foi extraído do vídeo")
            }

            println("📸 Extraídos ${frames.size} frames")

            val zipFilename = "frames_${video.uploadedAt.toString()}.zip"
            val zipPath = Paths.get("outputs", zipFilename).toFile()

            try {
                createZipFile.execute(frames, zipPath)
            } catch (e: IOException) {
                println("Erro ao criar arquivo ZIP: ${e.message}")
                return VideoResponseV1(
                id = video.id,
                status = VideoProcessStatusEnum.ERROR,
                message = "Failed to create ZIP file: ${e.message}"
            )
            }

            println("✅ ZIP criado: ${zipPath.absolutePath}")

            return VideoResponseV1(
                id = video.id,
                originalVideoPath = video.originalVideoPath,
                zipPath = zipPath.absolutePath,
                frameCount = frames.size,
                status = VideoProcessStatusEnum.FINISHED,
                message = "Video processing completed successfully"
            )

        } finally {
            tempDir.deleteRecursively()
        }
    }
}