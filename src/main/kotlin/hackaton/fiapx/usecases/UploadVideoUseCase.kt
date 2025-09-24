package hackaton.fiapx.usecases

import hackaton.fiapx.commons.dto.kafka.VideoUploadEvent
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.commons.interfaces.gateways.VideoEventGateway
import hackaton.fiapx.commons.interfaces.gateways.VideoGatewayInterface
import hackaton.fiapx.entities.Video
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Locale.getDefault

@Service
class UploadVideoUseCase(
    private val videoGateway: VideoGatewayInterface,
    private val videoEventGateway: VideoEventGateway
) {


    fun execute(videoFile: MultipartFile, userId: String? = null): Video {
        if (videoFile.isEmpty) {
            throw IllegalArgumentException("Erro ao receber arquivo de vídeo.")
        }

        if (!validateVideo(videoFile.originalFilename)) {
            throw IllegalArgumentException("Formato de arquivo não suportado. Use: .mp4, .avi, .mov, .mkv, .wmv, .flv, .webm")
        }

        val videoId = UUID.randomUUID()
        val timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val filename = "${timestamp}_${videoFile.originalFilename}"
        val uploadDir = Paths.get("uploads")
        val videoPath = uploadDir.resolve(filename)

        Files.createDirectories(uploadDir)

        videoFile.inputStream.use { input ->
            Files.newOutputStream(videoPath).use { output ->
                input.copyTo(output)
            }
        }

        val videoEntity = Video(
            id = videoId,
            originalVideoPath = videoPath.toString(),
            fileSize = videoFile.size,
            status = VideoProcessStatusEnum.UPLOADED,
            uploadedAt = OffsetDateTime.now()
        )

        val savedVideo = videoGateway.save(videoEntity)

        val uploadEvent = VideoUploadEvent(videoId = videoId)

        videoEventGateway.publishVideoUploadEvent(uploadEvent)

        return savedVideo
    }

    private fun validateVideo(filename: String?): Boolean {
        val validExtensions = listOf(".mp4", ".avi", ".mov", ".mkv", ".wmv", ".flv", ".webm")

        val lowercaseFilename = filename?.lowercase(getDefault())

        return lowercaseFilename?.let {
            validExtensions.any { extension -> it.endsWith(extension) }
        } ?: false
    }
}