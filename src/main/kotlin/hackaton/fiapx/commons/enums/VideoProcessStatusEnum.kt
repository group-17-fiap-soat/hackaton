package hackaton.fiapx.commons.enums

enum class VideoProcessStatusEnum {
    UPLOADED,   // Initial state when video is uploaded and event is produced
    PROCESSING, // When consumer starts processing the video
    FINISHED,   // When processing completes successfully
    ERROR       // When processing fails - video sent to DLQ
}