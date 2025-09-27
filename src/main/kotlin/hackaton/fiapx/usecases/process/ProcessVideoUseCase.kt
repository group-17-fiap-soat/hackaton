package hackaton.fiapx.usecases.process

import hackaton.fiapx.commons.dto.response.VideoResponseV1
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.entities.Video
import hackaton.fiapx.entities.User
import hackaton.fiapx.usecases.RedisCacheUseCase
import hackaton.fiapx.usecases.SendEmailUseCase
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.time.LocalDateTime

@Service
class ProcessVideoUseCase(
    private val createZipFile: CreateZipFileUseCase,
    private val sendEmail: SendEmailUseCase,
    private val redisCacheUseCase: RedisCacheUseCase
) {

    fun execute(video: Video, user: User): VideoResponseV1 {
        println("Iniciando processamento: ${video.originalVideoPath}")

        // Verificar se já está sendo processado (Redis Lock)
        if (!redisCacheUseCase.acquireProcessingLock(video.id!!)) {
            println("Vídeo ${video.id} já está sendo processado")
            redisCacheUseCase.cacheVideoProcessingStatus(video.id, "ALREADY_PROCESSING")
            return VideoResponseV1(status = VideoProcessStatusEnum.PROCESSING)
        }

        redisCacheUseCase.cacheVideoProcessingStatus(video.id, "PROCESSING")

        val timestamp = LocalDateTime.now().toString()
        val videoPath = video.originalVideoPath

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
                redisCacheUseCase.cacheVideoProcessingStatus(video.id, "ERROR_FFMPEG")
                return VideoResponseV1(status = VideoProcessStatusEnum.ERROR)
            }

            val frames = tempDir.listFiles { _, name -> name.endsWith(".png") }?.toList() ?: emptyList()

            if (frames.isEmpty()) {
                println("Nenhum frame foi extraído do vídeo")
                redisCacheUseCase.cacheVideoProcessingStatus(video.id, "ERROR_NO_FRAMES")

                val subject = "Falha no Processamento do Vídeo ${video.originalVideoPath} - FIAP X"
                val emailBody = """
                    Olá, ${user.name},

                    Houve um problema ao processar o seu vídeo "${video.originalVideoPath}".

                    O sistema não conseguiu extrair nenhum frame do arquivo enviado. Isso geralmente acontece por um dos seguintes motivos:
                    * O arquivo de vídeo está corrompido ou danificado.
                    * O formato do vídeo não é suportado pela nossa plataforma.
                    * O arquivo enviado está vazio ou não é um vídeo válido.

                    **O que fazer agora?**
                    Por favor, verifique a integridade do seu arquivo e tente enviá-lo novamente. Se o problema persistir, sinta-se à vontade para contatar o suporte.

                    Atenciosamente,
                    Equipe FIAP X
                """.trimIndent()

                sendEmail.execute(user.email!!, subject, emailBody)
                return VideoResponseV1(status = VideoProcessStatusEnum.ERROR)
            }

            println("📸 Extraídos ${frames.size} frames")

            val zipFilename = "frames_${timestamp}.zip"
            val zipPath = Paths.get("outputs", zipFilename).toFile()

            try {
                createZipFile.execute(frames, zipPath)
            } catch (e: IOException) {
                println("Erro ao criar arquivo ZIP: ${e.message}")
                redisCacheUseCase.cacheVideoProcessingStatus(video.id, "ERROR_ZIP_CREATION")

                val subject = "Falha no Processamento do Vídeo ${videoPath} - FIAP X"
                val emailBody = """
                    Olá, ${user.name},

                    Houve um problema ao finalizar o processamento do seu vídeo "$videoPath".

                    Os frames foram extraídos com sucesso, mas ocorreu um erro inesperado enquanto o sistema tentava compactá-los em um arquivo .zip.

                    **O que fazer agora?**
                    Esta parece ser uma falha interna. Nossa equipe já foi notificada para investigar a causa. Pedimos que, por favor, tente processar o vídeo novamente em alguns instantes.

                    Pedimos desculpas pelo inconveniente.

                    Atenciosamente,
                    Equipe FIAP X
                """.trimIndent()

                sendEmail.execute(user.email!!, subject, emailBody)
                return VideoResponseV1(status = VideoProcessStatusEnum.ERROR)
            }

            println("✅ ZIP criado: ${zipPath.absolutePath}")

            redisCacheUseCase.cacheVideoProcessingStatus(video.id, "SUCCESS")

            val subject = "Processamento de Vídeo Concluído com Sucesso - ${video.originalVideoPath} - FIAP X"
            val emailBody = """
                Olá, ${user.name},

                Ótimas notícias! O processamento do seu vídeo "${video.originalVideoPath}" foi concluído com sucesso.

                **Detalhes do processamento:**
                • Frames extraídos: ${frames.size}
                • Arquivo ZIP gerado: $zipFilename
                • Data de processamento: ${LocalDateTime.now()}

                O arquivo com todos os frames extraídos está disponível para download.

                Obrigado por usar nossa plataforma!

                Atenciosamente,
                Equipe FIAP X
            """.trimIndent()

            sendEmail.execute(user.email!!, subject, emailBody)

            return VideoResponseV1(
                zipPath = zipFilename,
                frameCount = frames.size,
                status = VideoProcessStatusEnum.FINISHED
            )

        } finally {
            redisCacheUseCase.releaseProcessingLock(video.id)
            tempDir.deleteRecursively()
        }
    }
}