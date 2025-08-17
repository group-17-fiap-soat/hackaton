package hackaton.fiapx.entities

import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import java.time.OffsetDateTime
import java.util.*

data class Video(
    val id: UUID? = null,
    val originalVideoPath: String? = null,
    val zipPath: String? = null,
    val frameCount: Int? = null,
    val fileSize: Long? = null,
    val status: VideoProcessStatusEnum? = null,
    val uploadedAt: OffsetDateTime? = null,
)
