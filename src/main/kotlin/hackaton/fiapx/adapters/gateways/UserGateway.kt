package hackaton.fiapx.adapters.gateways

import hackaton.fiapx.adapters.presenters.UserMapper
import hackaton.fiapx.commons.dao.UserDAO
import hackaton.fiapx.commons.interfaces.datasource.UserDataSource
import hackaton.fiapx.commons.interfaces.gateways.UserGatewayInterface
import hackaton.fiapx.entities.User
import org.springframework.stereotype.Component
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Component
class UserGateway(
    val userDataSource: UserDataSource,
) : UserGatewayInterface {

    override fun findById(id: UUID): User? {
        val userDAO: UserDAO? = userDataSource.findById(id).getOrNull()

        return userDAO?.let { UserMapper.fromDaoToEntity(it) }
    }

    override fun findByEmail(email: String): User? {
        val userDAO: UserDAO? = userDataSource.findByEmail(email).getOrNull()

        return userDAO?.let { UserMapper.fromDaoToEntity(it) }
    }

    override fun save(entity: User): User {
        val userEntity = UserMapper.toDAO(entity)

        return UserMapper.fromDaoToEntity(userDataSource.save(userEntity))
    }

}