package hackaton.fiapx.adapters.controllers.operation

import hackaton.fiapx.commons.dto.response.VideoResponseV1
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.security.Principal

interface VideoOperation {

    @Operation(
        summary = "Faz upload de um vídeo",
        description = "Recebe um arquivo de vídeo e cria um novo processamento para o usuário autenticado."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Vídeo enviado com sucesso",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = VideoResponseV1::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Arquivo inválido",
                content = [Content(mediaType = "application/json")]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Usuário não autenticado",
                content = [Content(mediaType = "application/json")]
            )
        ]
    )
    @PostMapping("/upload")
    fun upload(
        principal: Principal,
        @RequestParam("video") videoFile: MultipartFile
    ): ResponseEntity<VideoResponseV1>

    @Operation(
        summary = "Lista o status dos vídeos",
        description = "Retorna a lista de vídeos processados e seus status."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Lista retornada com sucesso",
                content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = VideoResponseV1::class)))]
            )
        ]
    )
    @GetMapping("/status")
    fun status(): ResponseEntity<List<VideoResponseV1>?>

    @Operation(
        summary = "Faz download de um vídeo",
        description = "Retorna o arquivo de vídeo pelo nome informado."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Download iniciado",
                content = [Content(mediaType = "application/octet-stream")]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Arquivo não encontrado",
                content = [Content(mediaType = "application/json")]
            )
        ]
    )
    @GetMapping("/download/{filename}")
    fun download(
        @PathVariable filename: String
    ): ResponseEntity<Resource>
}
