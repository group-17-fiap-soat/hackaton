package hackaton.fiapx.commons.dao

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

@Entity(name = "tb_user")
@EntityListeners(AuditingEntityListener::class)
data class UserDAO(

    @Id
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "name")
    val name: String? = null,

    @Column(name = "email", unique = true)
    val email: String? = null,

    @Column(name = "password")
    val passwordHash: String? = null

) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()
    override fun getPassword(): String? = passwordHash
    override fun getUsername(): String? = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
