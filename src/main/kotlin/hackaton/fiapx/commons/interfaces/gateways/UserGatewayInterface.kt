package hackaton.fiapx.commons.interfaces.gateways

import hackaton.fiapx.entities.User
import java.util.*

interface UserGatewayInterface {
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun save(entity: User): User
}