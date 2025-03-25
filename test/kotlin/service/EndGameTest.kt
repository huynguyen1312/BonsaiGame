package service

import entity.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

/**
 * Tests the endGame function of the [GameService].
 */
class EndGameTest {

    private lateinit var rootService: RootService
    private lateinit var refreshable: RefreshableTest

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
            GoalTile(5, 8, GoalTileType.WOOD), GoalTile(10, 10, GoalTileType.WOOD),
            GoalTile(15, 12, GoalTileType.WOOD), GoalTile(9, 3, GoalTileType.FRUIT),
            GoalTile(11, 4, GoalTileType.FRUIT), GoalTile(13, 5, GoalTileType.FRUIT),
            GoalTile(8, 3, GoalTileType.FLOWER), GoalTile(12, 4, GoalTileType.FLOWER),
            GoalTile(16, 5, GoalTileType.FLOWER)
        )

        val drawStack = mutableListOf<ZenCard>()

        val gameState = GameState(currentPlayers, 0, centerCards, States.CHOOSE_ACTION, goalTiles, drawStack)

        rootService.game = BonsaiGame(5.0, gameState)

        val currentGame = checkNotNull(rootService.game).currentState
        currentGame.state = States.GAME_ENDED

        val inventoryP1 = currentGame.players[0].storage
        val treeP1 = currentGame.players[0].tree

        val inventoryP2 = currentGame.players[1].storage
        val treeP2 = currentGame.players[1].tree

        treeP1.tiles[Vector(0, -1)] = BonsaiTileType.WOOD
        treeP1.tiles[Vector(1, -2)] = BonsaiTileType.WOOD
        treeP1.tiles[Vector(2, -3)] = BonsaiTileType.WOOD
        treeP1.tiles[Vector(3, -4)] = BonsaiTileType.WOOD
        treeP1.tiles[Vector(4, -5)] = BonsaiTileType.WOOD
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

        currentGame.players[0].discardPile.add(ParchmentCard(1, ParchmentCardType.WOOD, id = 50))

        refreshable = RefreshableTest()
        rootService.addRefreshable(refreshable)
    }

//    /**
//     * Tests for correct implementation of endGame, after it has been called
//     */
//    @Test
//    fun `test if last round is initiated correctly`() {
//        assertDoesNotThrow { rootService.gameService.endGame() }
//        val game = checkNotNull(rootService.game)
//        assertTrue(game.currentState.lastRound)
//        assertEquals(game.currentState.currentPlayerIndex, game.currentState.finalPlayer)
//    }

    /**
     * Tests for the correct declaration of the winner,
     * after the last round was played and the game has finished
     */
    @Test
    fun `test for correct declaration of winner`() {

        val game = checkNotNull(rootService.game)
        repeat(game.currentState.players.size) {
            game.currentState.state = States.CHOOSE_ACTION
            rootService.playerActionService.cultivate()
        }
        game.currentState.state = States.GAME_ENDED
        rootService.gameService.endGame()

        //Check for correct winner: player1

        assertTrue(refreshable.refreshAfterEndGameCalled)
    }

    /**
     * Tests for the correct declaration of the winner,
     * in an event of a tie,
     * after the last round was played and the game has finished
     */
    @Test
    fun `test for correct declaration of winner in tie event`() {
        val currentGame = checkNotNull(rootService.game).currentState
        currentGame.players[0].tree.tiles.remove(Vector(3, -4))
        currentGame.players[0].tree.tiles.remove(Vector(4, -5))


        val game = checkNotNull(rootService.game)
        repeat(game.currentState.players.size) {
            game.currentState.state = States.CHOOSE_ACTION
            rootService.playerActionService.cultivate()
        }
        game.currentState.state  = States.GAME_ENDED
        rootService.gameService.endGame()

        //Check for correct winner when tie

        assertTrue(refreshable.refreshAfterEndGameCalled)
    }

    /**
     * Tests for correct failure of endGame, if there is no game
     */
    @Test
    fun `test if no game is running`() {
        rootService.game = null
        assertFailsWith(IllegalStateException::class) { rootService.gameService.endGame() }
    }
}
