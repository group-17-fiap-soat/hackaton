package hackaton.fiapx.commons.interfaces.datasource

import hackaton.fiapx.commons.dao.VideoDAO
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface VideoDataSource : JpaRepository<VideoDAO, UUID> {
}