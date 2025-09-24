package hackaton.fiapx.adapters.presenters

import hackaton.fiapx.commons.dao.UserDAO
import hackaton.fiapx.commons.dto.request.AuthUserRequestV1
import hackaton.fiapx.commons.dto.request.RegisterUserRequestV1
import hackaton.fiapx.entities.User

object UserMapper {
    fun  fromRequestToDomain(request: RegisterUserRequestV1) =
        User(
            name = request.name,
            email = request.email,
            passwordHash = request.pass
        )

    fun  fromRequestToDomain(request: AuthUserRequestV1) =
        User(
            email = request.email,
            passwordHash = request.pass
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