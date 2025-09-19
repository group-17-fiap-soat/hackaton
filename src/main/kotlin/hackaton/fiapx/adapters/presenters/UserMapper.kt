package hackaton.fiapx.adapters.presenters

import hackaton.fiapx.commons.dao.UserDAO
import hackaton.fiapx.entities.User

object UserMapper {
    fun toEntity(dao: UserDAO) =
        User(
            id = dao.id,
            name = dao.name,
            email = dao.email,
            passwordHash = dao.passwordHash
        )

    fun toDAO(entity: User) =
        UserDAO(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            passwordHash = entity.passwordHash
        )

    fun fromDaoToEntity(dao: UserDAO) =
        User(
            id = dao.id,
            name = dao.name,
            email = dao.email,
            passwordHash = dao.passwordHash

        )
}