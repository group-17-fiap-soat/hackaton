package hackaton.fiapx.commons.interfaces.services

interface DlqService {
    fun processDlqMessage(topic: String, dlqEvent: Map<String, Any>, correlationId: String?)
}