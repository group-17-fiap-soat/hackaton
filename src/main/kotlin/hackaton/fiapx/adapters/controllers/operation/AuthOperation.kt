package hackaton.fiapx.adapters.controllers.operation

import hackaton.fiapx.commons.dto.request.AuthUserRequestV1
import hackaton.fiapx.commons.dto.request.RegisterUserRequestV1
import hackaton.fiapx.commons.dto.response.AuthUserResponseV1
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

interface AuthOperation {

    @Operation(
        summary = "Registra um novo usuário",
        description = "Recebe os dados do usuário no corpo da requisição e cria um novo registro."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Usuário registrado com sucesso",
                content = [Content(mediaType = "application/json")]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Usuário já existe ou dados inválidos",
                content = [Content(mediaType = "application/json")]
            )
        ]
    )
    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterUserRequestV1
    ): ResponseEntity<String>

    @Operation(
        summary = "Autentica um usuário",
        description = "Autentica um usuário com as credenciais fornecidas e retorna um token JWT em caso de sucesso."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Autenticação bem-sucedida",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AuthUserResponseV1::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Credenciais inválidas",
                content = [Content(mediaType = "application/json")]
            )
        ]
    )
    @PostMapping("/login")
    fun login(
        @RequestBody request: AuthUserRequestV1
    ): ResponseEntity<Any>
}