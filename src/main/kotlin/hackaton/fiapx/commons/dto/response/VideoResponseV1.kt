package hackaton.fiapx.commons.dto.response

import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import java.time.OffsetDateTime
import java.util.*

data class VideoResponseV1(
    val id: UUID? = null,
    val originalVideoPath: String? = null,
    val zipPath: String? = null,
    val frameCount: Int? = null,
    val fileSize: Long? = null,
    val status: VideoProcessStatusEnum? = null,
    val uploadedAt: OffsetDateTime? = null,
)
