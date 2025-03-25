package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Test class for the method [TreeService.getPossiblePlacements].
 */
class GetPossiblePlacementsTest {

    private lateinit var rootService: RootService
    private lateinit var treeService: TreeService

    /**
     * Initializes a valid game state before each test
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        val gameConfig = GameConfig(
            playerName = mutableListOf("Alice", "Bob"),
            playerTypes = mutableListOf(PlayerType.LOCAL, PlayerType.LOCAL),
            color = mutableListOf(Colors.RED, Colors.BLUE),
            goalTiles = mutableListOf(GoalTileType.WOOD, GoalTileType.LEAF, GoalTileType.FRUIT)
        )
        rootService.gameService.startGame(gameConfig)
        treeService = TreeService(rootService)
    }

    /**
     * Tests that `getPossiblePlacements()` correctly identifies available positions around an existing tree.
     */
    @Test
    fun `possible placements around an existing tree`() {
        val player = rootService.game!!.currentState.currentPlayer
        player.tree.tiles[Vector(0, -1)] = BonsaiTileType.WOOD
        player.tree.tiles[Vector(1, -2)] = BonsaiTileType.LEAF
        player.tree.tiles[Vector(0, -2)] = BonsaiTileType.LEAF

        val possiblePositions = treeService.getPossiblePlacements()

        val expectedPositions = listOf(
            Vector(-1, -1), Vector(-1, -2), Vector(0, -3), Vector(1, -3),
            Vector(2, -3), Vector(2, -2), Vector(1, -1)
        )

        assertEquals(expectedPositions.size, possiblePositions.size, "Unexpected number of possible placements.")

        expectedPositions.forEach { vector ->
            assertContains(possiblePositions, vector, "Expected position $vector not found.")
        }
    }

    /**
     * Tests that `getPossiblePlacements()` returns a list of two elements when the tree has no tiles.
     */
    @Test
    fun `possible placements on an empty tree`() {
        val player = rootService.game!!.currentState.currentPlayer
        player.tree.tiles.clear()

        val possiblePositions = treeService.getPossiblePlacements()

        assertEquals(2, possiblePositions.size, "Expected exactly 2 possible placements on an empty tree.")
    }

    /**
     * Tests that `getPossiblePlacements()` throws an exception when no game is active.
     */
    @Test
    fun `throws IllegalStateException if no active game`() {
        rootService.game = null
        assertThrows<IllegalStateException> {
            treeService.getPossiblePlacements()
        }
    }
}
