package hackaton.fiapx.commons.dao

import hackaton.fiapx.commons.enums.VideoProcessStatusEnum
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime
import java.util.*

@Entity(name = "tb_video")
@EntityListeners(AuditingEntityListener::class)
data class VideoDAO(

    @Id
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "user_id")
    val userId: UUID? = null,

    @Column(name = "original_video_path")
    val originalVideoPath: String? = null,

    @Column(name = "zip_path")
    val zipPath: String? = null,

    @Column(name = "frame_count")
    val frameCount: Int? = null,

    @Column(name = "file_size")
    val fileSize: Long? = null,

    @Column(name = "status")
    val status: VideoProcessStatusEnum? = null,

    @CreationTimestamp
    @Column(name = "uploaded_at")
    val uploadedAt: OffsetDateTime? = null,
)