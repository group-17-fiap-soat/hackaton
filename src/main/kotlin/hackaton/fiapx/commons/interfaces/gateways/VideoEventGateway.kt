package hackaton.fiapx.commons.interfaces.gateways

interface VideoEventGateway {

    fun publishToProcessingTopic(event: Any, eventType: String, videoId: String)
    fun sendToDlq(originalEvent: Any, exception: Throwable)
}