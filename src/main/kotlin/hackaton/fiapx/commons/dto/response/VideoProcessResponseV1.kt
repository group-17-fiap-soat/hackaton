package hackaton.fiapx.commons.dto.response

import hackaton.fiapx.commons.enums.VideoProcessStatusEnum

data class VideoProcessResponseV1(
    val zipPath: String? = null,
    val frameCount: Int? = null,
    val status: VideoProcessStatusEnum? = null
)
