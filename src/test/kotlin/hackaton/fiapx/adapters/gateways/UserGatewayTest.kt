package hackaton.fiapx.adapters.gateways

import hackaton.fiapx.commons.dao.UserDAO
import hackaton.fiapx.commons.interfaces.datasource.UserDataSource
import hackaton.fiapx.entities.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import java.util.*
import java.util.function.Function

class UserGatewayTest {

    private class FakeUserDataSource : UserDataSource {
        var savedUser: UserDAO? = null
        var userToReturn: UserDAO? = null
        var userById: UserDAO? = null

        override fun findById(id: UUID): Optional<UserDAO> {
            return Optional.ofNullable(userById)
        }

        override fun findByEmail(email: String): Optional<UserDAO> {
            return Optional.ofNullable(userToReturn)
        }

        override fun <S : UserDAO> save(entity: S): S {
            savedUser = entity
            return entity
        }

        // Minimal JpaRepository method implementations for compilation
        override fun findAll(): MutableList<UserDAO> = mutableListOf()
        override fun findAll(sort: Sort): MutableList<UserDAO> = mutableListOf()
        override fun findAllById(ids: MutableIterable<UUID>): MutableList<UserDAO> = mutableListOf()
        override fun <S : UserDAO> saveAll(entities: MutableIterable<S>): MutableList<S> = mutableListOf()
        override fun existsById(id: UUID): Boolean = false
        override fun count(): Long = 0
        override fun deleteById(id: UUID) {}
        override fun delete(entity: UserDAO) {}
        override fun deleteAllById(ids: MutableIterable<UUID>) {}
        override fun deleteAll(entities: MutableIterable<UserDAO>) {}
        override fun deleteAll() {}
        override fun flush() {}
        override fun <S : UserDAO> saveAndFlush(entity: S): S = entity
        override fun <S : UserDAO> saveAllAndFlush(entities: MutableIterable<S>): MutableList<S> = mutableListOf()
        override fun deleteAllInBatch(entities: MutableIterable<UserDAO>) {}
        override fun deleteAllByIdInBatch(ids: MutableIterable<UUID>) {}
        override fun deleteAllInBatch() {}
        override fun getOne(id: UUID): UserDAO = UserDAO()
        override fun getById(id: UUID): UserDAO = UserDAO()
        override fun getReferenceById(id: UUID): UserDAO = UserDAO()
        override fun findAll(pageable: Pageable): Page<UserDAO> = Page.empty()
        override fun <S : UserDAO> findOne(example: Example<S>): Optional<S> = Optional.empty()
        override fun <S : UserDAO> findAll(example: Example<S>): MutableList<S> = mutableListOf()
        override fun <S : UserDAO> findAll(example: Example<S>, sort: Sort): MutableList<S> = mutableListOf()
        override fun <S : UserDAO> findAll(example: Example<S>, pageable: Pageable): Page<S> = Page.empty()
        override fun <S : UserDAO> count(example: Example<S>): Long = 0
        override fun <S : UserDAO> exists(example: Example<S>): Boolean = false
        override fun <S : UserDAO, R : Any> findBy(example: Example<S>, queryFunction: Function<FluentQuery.FetchableFluentQuery<S>, R>): R {
            throw UnsupportedOperationException()
        }
    }

    @Test
    fun findByIdReturnsUserWhenExists() {
        val userId = UUID.randomUUID()
        val userDAO = UserDAO(userId, "John Doe", "john@test.com", "hashedPassword")
        val dataSource = FakeUserDataSource().apply { userById = userDAO }
        val gateway = UserGateway(dataSource)

        val result = gateway.findById(userId)

        org.junit.jupiter.api.Assertions.assertNotNull(result)
        assertEquals(userId, result?.id)
        assertEquals("John Doe", result?.name)
        assertEquals("john@test.com", result?.email)
    }

    @Test
    fun findByIdReturnsNullWhenNotExists() {
        val dataSource = FakeUserDataSource()
        val gateway = UserGateway(dataSource)

        val result = gateway.findById(UUID.randomUUID())

        org.junit.jupiter.api.Assertions.assertNull(result)
    }

    @Test
    fun findByEmailReturnsUserWhenExists() {
        val userDAO = UserDAO(UUID.randomUUID(), "Jane Doe", "jane@test.com", "hashedPassword")
        val dataSource = FakeUserDataSource().apply { userToReturn = userDAO }
        val gateway = UserGateway(dataSource)

        val result = gateway.findByEmail("jane@test.com")

        org.junit.jupiter.api.Assertions.assertNotNull(result)
        assertEquals("Jane Doe", result?.name)
        assertEquals("jane@test.com", result?.email)
    }

    @Test
    fun findByEmailReturnsNullWhenNotExists() {
        val dataSource = FakeUserDataSource()
        val gateway = UserGateway(dataSource)

        val result = gateway.findByEmail("nonexistent@test.com")

        org.junit.jupiter.api.Assertions.assertNull(result)
    }

    @Test
    fun saveCreatesNewUserWithGeneratedId() {
        val dataSource = FakeUserDataSource()
        val gateway = UserGateway(dataSource)
        val user = User(null, "New User", "new@test.com", "password")

        val result = gateway.save(user)

        assertEquals("New User", result.name)
        assertEquals("new@test.com", result.email)
        org.junit.jupiter.api.Assertions.assertNotNull(dataSource.savedUser)
    }

    @Test
    fun saveUpdatesExistingUser() {
        val existingId = UUID.randomUUID()
        val dataSource = FakeUserDataSource()
        val gateway = UserGateway(dataSource)
        val user = User(existingId, "Updated User", "updated@test.com", "newPassword")

        val result = gateway.save(user)

        assertEquals(existingId, result.id)
        assertEquals("Updated User", result.name)
        assertEquals("updated@test.com", result.email)
        org.junit.jupiter.api.Assertions.assertNotNull(dataSource.savedUser)
        assertEquals(existingId, dataSource.savedUser?.id)
    }

    @Test
    fun saveHandlesUserWithNullFields() {
        val dataSource = FakeUserDataSource()
        val gateway = UserGateway(dataSource)
        val user = User(null, null, null, null)

        val result = gateway.save(user)

        org.junit.jupiter.api.Assertions.assertNull(result.name)
        org.junit.jupiter.api.Assertions.assertNull(result.email)
        org.junit.jupiter.api.Assertions.assertNotNull(dataSource.savedUser)
    }
}
