package hackaton.fiapx.adapters.presenters

import hackaton.fiapx.commons.dao.VideoDAO
import hackaton.fiapx.commons.dto.response.VideoResponseV1
import hackaton.fiapx.entities.Video
import java.util.UUID

object VideoMapper {
    fun toEntity(dao: VideoDAO) =
        Video(
            id = dao.id,
            userId = dao.userId,
            originalVideoPath = dao.originalVideoPath,
            zipPath = dao.zipPath,
            frameCount = dao.frameCount,
            fileSize = dao.fileSize,
            status = dao.status,
            uploadedAt = dao.uploadedAt
        )

    fun toDAO(entity: Video) =
        VideoDAO(
            id = entity.id,
            userId = entity.userId,
            originalVideoPath = entity.originalVideoPath,
            zipPath = entity.zipPath,
            frameCount = entity.frameCount,
            fileSize = entity.fileSize,
            status = entity.status,
            uploadedAt = entity.uploadedAt,
        )

    fun fromDaoToEntity(dao: VideoDAO) =
        Video(
            id = dao.id,
            userId = dao.userId,
            originalVideoPath = dao.originalVideoPath,
            zipPath = dao.zipPath,
            frameCount = dao.frameCount,
            fileSize = dao.fileSize,
            status = dao.status,
            uploadedAt = dao.uploadedAt,
        )

    fun toVideoResponseV1(entity: Video) =
        VideoResponseV1(
            id = entity.id,
            userId = entity.userId,
            originalVideoPath = entity.originalVideoPath,
            zipPath = entity.zipPath,
            frameCount = entity.frameCount,
            fileSize = entity.fileSize,
            uploadedAt = entity.uploadedAt,
            status = entity.status,
            message = entity.errorMessage
        )
}
