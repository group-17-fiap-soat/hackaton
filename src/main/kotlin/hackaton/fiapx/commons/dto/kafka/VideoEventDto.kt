package hackaton.fiapx.commons.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes(
    JsonSubTypes.Type(value = VideoUploadEvent::class, name = "video_upload")
)
sealed class VideoEvent

data class VideoUploadEvent(
    @JsonProperty("videoId") val videoId: UUID,
) : VideoEvent()