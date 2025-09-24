package hackaton.fiapx.commons.dto.response

data class AuthUserResponseV1(
    val accessToken: String,
    val tokenType: String = "Bearer"
)