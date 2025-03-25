import entity.ToolCard
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TestToolCard tests the ToolCard Entity.
 */
class ToolCardTest {

    /**
     * Tests if equals works as expected
     */
    @Test
    fun `test toolCard equals`() {
        // test toolCard
        assertEquals(ToolCard(1, id = 10), ToolCard(1, id = 10))
        assertEquals(ToolCard(42, id = 12), ToolCard(42, id = 12))
    }
}