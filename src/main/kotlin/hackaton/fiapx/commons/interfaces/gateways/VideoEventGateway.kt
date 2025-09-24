package hackaton.fiapx.commons.interfaces.gateways

import hackaton.fiapx.commons.dto.kafka.VideoUploadEvent

interface VideoEventGateway {

    fun publishVideoUploadEvent(event: VideoUploadEvent)
    fun sendToDlq(originalEvent: Any, exception: Throwable)
}