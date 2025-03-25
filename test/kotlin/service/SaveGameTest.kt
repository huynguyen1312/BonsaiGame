package service

import entity.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import kotlin.test.assertFailsWith

/**
 * Tests the saveGame function of the [FileService].
 */
class SaveGameTest {

    private lateinit var rootService: RootService

    /**
     * set up a running game
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()

        val currentPlayers = arrayOf(
            Player("Max", PlayerType.LOCAL, Colors.RED),
            Player("Alex", PlayerType.EASY_BOT, Colors.BLUE)
        )

        val centerCards : Array<ZenCard?> = arrayOf(
            GrowthCard(BonsaiTileType.LEAF, id = 42),
            ToolCard(id = 43),
            HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.FRUIT), id = 44),
            MasterCard(arrayOf(BonsaiTileType.ANY), id = 45)
        )
        centerCards.shuffle()

        val goalTiles = mutableListOf(
            GoalTile(5, 8, GoalTileType.WOOD),
            GoalTile(10, 10, GoalTileType.WOOD),
            GoalTile(15, 12, GoalTileType.WOOD),
            GoalTile(9, 3, GoalTileType.FRUIT),
            GoalTile(11, 4, GoalTileType.FRUIT),
            GoalTile(13, 5, GoalTileType.FRUIT),
            GoalTile(8, 3, GoalTileType.FLOWER),
            GoalTile(12, 4, GoalTileType.FLOWER),
            GoalTile(16, 5, GoalTileType.FLOWER)
        )

        val drawStack = mutableListOf<ZenCard>()

        val gameState = GameState(currentPlayers, 0, centerCards, States.CHOOSE_ACTION, goalTiles, drawStack)

        rootService.game = BonsaiGame(5.0, gameState)

        val currentGame = checkNotNull(rootService.game).currentState

        val inventoryP1 = currentGame.players[0].storage
        val treeP1 = currentGame.players[0].tree

        val inventoryP2 = currentGame.players[1].storage
        val treeP2 = currentGame.players[1].tree

        treeP1.tiles[Vector(0, -1)] = BonsaiTileType.WOOD
        treeP1.tiles[Vector(1, -2)] = BonsaiTileType.WOOD
        treeP1.tiles[Vector(2, -3)] = BonsaiTileType.WOOD
        treeP1.tiles[Vector(2, -4)] = BonsaiTileType.LEAF
        treeP1.tiles[Vector(1, -3)] = BonsaiTileType.LEAF
        treeP1.tiles[Vector(1, -4)] = BonsaiTileType.FRUIT
        treeP1.tiles[Vector(2, -5)] = BonsaiTileType.FLOWER

        treeP2.tiles[Vector(1, -1)] = BonsaiTileType.WOOD
        treeP2.tiles[Vector(1, -2)] = BonsaiTileType.WOOD
        treeP2.tiles[Vector(1, -3)] = BonsaiTileType.WOOD
        treeP1.tiles[Vector(2, -4)] = BonsaiTileType.LEAF
        treeP1.tiles[Vector(2, -3)] = BonsaiTileType.LEAF
        treeP1.tiles[Vector(3, -4)] = BonsaiTileType.FRUIT
        treeP1.tiles[Vector(3, -5)] = BonsaiTileType.FLOWER

        inventoryP1.addAll(
            mutableListOf(
                BonsaiTile(BonsaiTileType.WOOD), BonsaiTile(BonsaiTileType.LEAF), BonsaiTile(BonsaiTileType.WOOD)
            )
        )
        inventoryP2.addAll(
            mutableListOf(
                BonsaiTile(BonsaiTileType.FRUIT), BonsaiTile(BonsaiTileType.FLOWER), BonsaiTile(BonsaiTileType.WOOD)
            )
        )
    }

    /**
     * Tests the correct functionality of saveGame
     */
    @Test
    fun `test correct operation of saveGame`() {
        assertDoesNotThrow { rootService.fileService.saveGame() }
        // I adjusted the path so it isn't stored in the root directory
        val file = File("./saves/SaveGame.json")
        assertTrue(file.exists())
    }

    /**
     * Tests the failure of saveGame if there is no game
     */
    @Test
    fun `tests if no active game`() {
        rootService.game = null

        assertFailsWith(IllegalStateException::class) { rootService.fileService.saveGame() }
    }
}
