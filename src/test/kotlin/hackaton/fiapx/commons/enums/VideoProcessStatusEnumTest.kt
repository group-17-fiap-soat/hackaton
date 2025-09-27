package hackaton.fiapx.commons.enums

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VideoProcessStatusEnumTest {

    @Test
    fun `contains all expected status values`() {
        val expectedValues = setOf("UPLOADED", "PROCESSING", "FINISHED", "ERROR")
        val actualValues = VideoProcessStatusEnum.values().map { it.name }.toSet()

        assertEquals(expectedValues, actualValues)
        assertEquals(4, VideoProcessStatusEnum.values().size)
    }

    @Test
    fun `status transitions follow expected workflow`() {
        val ordinalValues = VideoProcessStatusEnum.values().associateWith { it.ordinal }

        assertTrue(ordinalValues[VideoProcessStatusEnum.UPLOADED]!! < ordinalValues[VideoProcessStatusEnum.PROCESSING]!!)
        assertTrue(ordinalValues[VideoProcessStatusEnum.PROCESSING]!! < ordinalValues[VideoProcessStatusEnum.FINISHED]!!)
    }

    @Test
    fun `can convert from string values`() {
        assertEquals(VideoProcessStatusEnum.UPLOADED, VideoProcessStatusEnum.valueOf("UPLOADED"))
        assertEquals(VideoProcessStatusEnum.PROCESSING, VideoProcessStatusEnum.valueOf("PROCESSING"))
        assertEquals(VideoProcessStatusEnum.FINISHED, VideoProcessStatusEnum.valueOf("FINISHED"))
        assertEquals(VideoProcessStatusEnum.ERROR, VideoProcessStatusEnum.valueOf("ERROR"))
    }

    @Test
    fun `handles case sensitive string conversion`() {
        try {
            VideoProcessStatusEnum.valueOf("uploaded")
            assert(false) { "Should have thrown IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // Expected behavior
        }
    }

    @Test
    fun `provides meaningful string representation`() {
        assertEquals("UPLOADED", VideoProcessStatusEnum.UPLOADED.toString())
        assertEquals("PROCESSING", VideoProcessStatusEnum.PROCESSING.toString())
        assertEquals("FINISHED", VideoProcessStatusEnum.FINISHED.toString())
        assertEquals("ERROR", VideoProcessStatusEnum.ERROR.toString())
    }

    @Test
    fun `supports equality comparison`() {
        val status1 = VideoProcessStatusEnum.UPLOADED
        val status2 = VideoProcessStatusEnum.UPLOADED
        val status3 = VideoProcessStatusEnum.PROCESSING

        assertEquals(status1, status2)
        assertTrue(status1 != status3)
        assertEquals(status1.hashCode(), status2.hashCode())
        assertTrue(status1.hashCode() != status3.hashCode())
    }

    @Test
    fun `can be used in when expressions`() {
        val testStatuses = VideoProcessStatusEnum.values()

        testStatuses.forEach { status ->
            val result = when (status) {
                VideoProcessStatusEnum.UPLOADED -> "Initial state"
                VideoProcessStatusEnum.PROCESSING -> "In progress"
                VideoProcessStatusEnum.FINISHED -> "Complete"
                VideoProcessStatusEnum.ERROR -> "Failed"
            }

            assertTrue(result.isNotEmpty())
        }
    }

    @Test
    fun `can be used in collections`() {
        val successStates = setOf(VideoProcessStatusEnum.UPLOADED, VideoProcessStatusEnum.FINISHED)
        val failureStates = setOf(VideoProcessStatusEnum.ERROR)
        val inProgressStates = setOf(VideoProcessStatusEnum.PROCESSING)

        assertTrue(successStates.contains(VideoProcessStatusEnum.UPLOADED))
        assertTrue(failureStates.contains(VideoProcessStatusEnum.ERROR))
        assertTrue(inProgressStates.contains(VideoProcessStatusEnum.PROCESSING))
        assertTrue(!successStates.contains(VideoProcessStatusEnum.ERROR))
    }

    @Test
    fun `supports iteration over all values`() {
        var count = 0
        for (status in VideoProcessStatusEnum.values()) {
            count++
            assertTrue(status.name.isNotEmpty())
        }
        assertEquals(4, count)
    }

    @Test
    fun `maintains consistent ordinal values`() {
        assertEquals(0, VideoProcessStatusEnum.UPLOADED.ordinal)
        assertEquals(1, VideoProcessStatusEnum.PROCESSING.ordinal)
        assertEquals(2, VideoProcessStatusEnum.FINISHED.ordinal)
        assertEquals(3, VideoProcessStatusEnum.ERROR.ordinal)
    }
}
