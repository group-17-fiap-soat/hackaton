package hackaton.fiapx.adapters.controllers

import hackaton.fiapx.adapters.presenters.VideoMapper
import hackaton.fiapx.commons.dto.response.VideoResponseV1
import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import hackaton.fiapx.usecases.DownloadVideoUseCase
import hackaton.fiapx.usecases.ListVideoUseCase
import hackaton.fiapx.usecases.UploadVideoUseCase
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api")
class VideoController(
    private val uploadVideo: UploadVideoUseCase,
    private val listVideo: ListVideoUseCase,
    private val downloadVideo: DownloadVideoUseCase
) {
    private val logger = LoggerFactory.getLogger(VideoController::class.java)

    @PostMapping("/upload")
    fun upload(
        @RequestParam("video") videoFile: MultipartFile,
        @RequestParam("userId", required = false) userId: String?,
        @RequestHeader("X-Correlation-ID", required = false) correlationId: String?
    ): ResponseEntity<VideoResponseV1> {

        val requestId = correlationId ?: UUID.randomUUID().toString()
        val actualUserId = userId ?: "anonymous"

        logger.info("Video upload request received - RequestId: $requestId, UserId: $actualUserId, FileName: ${videoFile.originalFilename}")

        try {
            val savedVideo = uploadVideo.execute(videoFile, actualUserId)

            val response = VideoMapper.toVideoResponseV1(savedVideo).copy(
                status = VideoProcessStatusEnum.UPLOADED,
                message = "Video uploaded successfully and queued for processing"
            )

            logger.info("Video upload successful - VideoId: ${savedVideo.id}, RequestId: $requestId")
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response)

        } catch (exception: Exception) {
            logger.error("Video upload failed - RequestId: $requestId, UserId: $actualUserId", exception)

            val errorResponse = VideoResponseV1(
                id = null,
                message = "Upload failed: ${exception.message}",
                status = VideoProcessStatusEnum.ERROR
            )

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
        }
    }

    @GetMapping("/status")
    fun status(): ResponseEntity<List<VideoResponseV1>?> {
        val videos = listVideo.execute()
        val response = videos.map(VideoMapper::toVideoResponseV1)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/download")
    fun download(@RequestParam filename: String): ResponseEntity<Resource> {
        val file = downloadVideo.execute(filename)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        val resource: Resource = FileSystemResource(file)

        return ResponseEntity.status(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${file.name}\"")
            .contentLength(file.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }
}