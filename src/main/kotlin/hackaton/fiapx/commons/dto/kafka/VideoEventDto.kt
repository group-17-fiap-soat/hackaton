package hackaton.fiapx.commons.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes(
    JsonSubTypes.Type(value = VideoEventDto::class, name = "video_upload")
)

data class VideoEventDto(
    @JsonProperty("videoId") val videoId: UUID,
    @JsonProperty("userId") val userId: UUID,
    @JsonProperty("userEmail") val userEmail: String,
    @JsonProperty("userName") val userName: String?
)