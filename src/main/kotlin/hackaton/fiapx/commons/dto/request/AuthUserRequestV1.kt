package hackaton.fiapx.commons.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull

data class AuthUserRequestV1(
    @Email(message = "O e-mail precisa ser válido.")
    var email: String? = null,

    @NotNull(message = "O campo não pode ser nulo.")
    var pass: String? = null
)
