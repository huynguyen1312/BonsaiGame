package service

import entity.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFailsWith

/**
 * Tests the loadGame function of the [FileService].
 */
class LoadGameTest {

    private lateinit var rootService: RootService
    private lateinit var currentPlayers: Array<Player>
    private lateinit var centerCards: Array<ZenCard?>
    private lateinit var goalTiles: MutableList<GoalTile>
    private lateinit var currentState: States

    /**
     * set up a running game
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()

        currentPlayers = arrayOf(
            Player("Max", PlayerType.LOCAL, Colors.RED),
            Player("Alex", PlayerType.EASY_BOT, Colors.BLUE)
        )

        centerCards = arrayOf(
            GrowthCard(BonsaiTileType.LEAF, id = 42),
            ToolCard(id = 43),
            HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.FRUIT), id = 44),
            MasterCard(arrayOf(BonsaiTileType.ANY), id = 45)
        )
        centerCards.shuffle()

        goalTiles = mutableListOf(
            GoalTile(5, 8, GoalTileType.WOOD), GoalTile(10, 10, GoalTileType.WOOD),
            GoalTile(15, 12, GoalTileType.WOOD), GoalTile(9, 3, GoalTileType.FRUIT),
            GoalTile(11, 4, GoalTileType.FRUIT), GoalTile(13, 5, GoalTileType.FRUIT),
            GoalTile(8, 3, GoalTileType.FLOWER), GoalTile(12, 4, GoalTileType.FLOWER),
            GoalTile(16, 5, GoalTileType.FLOWER)
        )

        val drawStack = mutableListOf<ZenCard>()
        currentState = States.CHOOSE_ACTION

        val gameState = GameState(currentPlayers, 0, centerCards, currentState, goalTiles, drawStack)

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

        rootService.fileService.saveGame()
        rootService.game = null
    }

    /**
     * Tests the correct functionality of loadGame
     */
    @Test
    fun `test correct operation of loadGame`() {
        val refreshable = RefreshableTest()
        assertNull(rootService.game)

        rootService.fileService.addRefreshable(refreshable)
        assertDoesNotThrow { rootService.fileService.loadGame() }
        assertTrue(refreshable.refreshAfterLoadGameCalled)
        assertNotNull(rootService.game)

        val game = checkNotNull(rootService.game)

        currentPlayers.forEachIndexed { index, player ->
            assertEquals(player, game.currentState.players[index])
        }

        assertArrayEquals(centerCards, game.currentState.centerCards)
        assertEquals(currentState, game.currentState.state)
        assertEquals(goalTiles, game.currentState.goalTiles)
    }

    /**
     * Tests the failure of loadGame if there is no save game file
     */
    @Test
    fun `test if no save game file exists`() {
        val file = File("./saves/SaveGame.json")
        file.delete()

        assertFailsWith(IllegalStateException::class) { rootService.fileService.loadGame() }
    }
}
