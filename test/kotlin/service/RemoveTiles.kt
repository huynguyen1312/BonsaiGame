package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests the removeTiles function of the TreeService.
 */
class RemoveTiles {

    private lateinit var root: RootService
    private lateinit var gameConfig: GameConfig
    private lateinit var game: GameState

    /**
     * Set up the test environment.
     */
    @BeforeEach
    fun setup() {
        root = RootService()
        gameConfig = GameConfig(
            2.0,
            mutableListOf("John", "Joe"),
            mutableListOf(PlayerType.LOCAL, PlayerType.EASY_BOT),
            false,
            false,
            false,
            mutableListOf(GoalTileType.WOOD, GoalTileType.LEAF, GoalTileType.FRUIT),
            mutableListOf(Colors.PURPLE, Colors.BLACK)
        )
        root.gameService.startGame(gameConfig)
        game = requireNotNull(root.game) { "Expected game to be in progress" }.currentState
        game.state = States.REMOVE_TILES
    }


    /**
     * Tests the success of the removeTiles function of the TreeService & the validateRemoveTiles function of the Tree.
     */
    @Test
    fun `test removeTiles when preconditions are met`() {
        //set up Game State
        val refreshableTest = RefreshableTest()
        root.addRefreshable(refreshableTest)
        //set up Tree
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(0, -1), BonsaiTileType.WOOD)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(-1,-1), BonsaiTileType.LEAF)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(0,-2), BonsaiTileType.LEAF)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(1,-2), BonsaiTileType.LEAF)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(1,-1), BonsaiTileType.LEAF)

        //canRemoveTiles true
        assertTrue(root.treeService.validateRemoveTiles(arrayOf(Vector(-1,-1)), true))
        assertTrue(root.treeService.validateRemoveTiles(arrayOf(Vector(0,-2)), false))
        assertTrue(root.treeService.validateRemoveTiles(arrayOf(Vector(1,-2)), false))
        assertTrue(root.treeService.validateRemoveTiles(arrayOf(Vector(1,-1)), false))

        //removeTiles
        root.treeService.removeTiles(arrayOf(Vector(0,-2)))
        //check if tiles are away
        assertFalse(game.currentPlayer.tree.tiles.containsKey(Vector(0,-2)))
        //check state changed
        assertEquals(game.state, States.CHOOSE_ACTION)
        //check refreshCalled
        assertTrue(refreshableTest.refreshAfterTilesRemovedCalled)
    }

    /**
     * Tests the failure of the removeTiles function of the TreeService & the validateRemoveTiles function of the Tree.
     */
    @Test
    fun `test removeTiles when preconditions are not met`() {
        val root1 = RootService()
        //game is not Running
        assertThrows<IllegalStateException> {
            root1.treeService.removeTiles(arrayOf(Vector(0,-1)))
        }
        //set up Game State
        //set up Tree
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(0, -1), BonsaiTileType.WOOD)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(-1,-1), BonsaiTileType.LEAF)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(0,-2), BonsaiTileType.LEAF)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(1,-2), BonsaiTileType.LEAF)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(1,-1), BonsaiTileType.LEAF)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(1,-3), BonsaiTileType.FRUIT)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(2,-2), BonsaiTileType.FLOWER)
        game.currentPlayer.tree.tiles.putIfAbsent(Vector(-1,-2), BonsaiTileType.FLOWER)
        //not minimal tiles
        assertFalse(root.treeService.validateRemoveTiles(arrayOf(Vector(0,-2),Vector(-1,-2)), false))
        //validateRemoveTiles false/throws exception
        assertThrows<IllegalArgumentException> {
            root.treeService.validateRemoveTiles(arrayOf(Vector(0,-2),Vector(-1,-2)), true)
        }
        assertTrue(root.treeService.validateRemoveTiles(arrayOf(Vector(-1,-1)), false))
        //no tiles at position or unremovable Tile
        assertFalse(root.treeService.validateRemoveTiles(arrayOf(Vector(0,0)), false))
        assertFalse(root.treeService.validateRemoveTiles(arrayOf(Vector(100,100)), false))
        //not the right state
        game.state = States.MEDITATE
        assertFalse(root.treeService.validateRemoveTiles(arrayOf(Vector(1,-2)), false))
    }
}
