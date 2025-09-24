package hackaton.fiapx.commons.interfaces.datasource

import hackaton.fiapx.commons.dao.UserDAO
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserDataSource : JpaRepository<UserDAO, UUID> {
    fun findByEmail(email: String): Optional<UserDAO>
}