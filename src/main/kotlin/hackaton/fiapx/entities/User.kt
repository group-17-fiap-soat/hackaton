package hackaton.fiapx.entities

import java.time.OffsetDateTime
import java.util.*

data class User(
    val id: UUID? = null,
    val name: String? = null,
    val email: String? = null,
    val passwordHash: String? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)
