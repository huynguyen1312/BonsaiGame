package service

import entity.Colors
import entity.GoalTileType
import entity.PlayerType
import entity.ToolCard
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

/**
 * Tests the startGame function of the GameService.
 */
class StartGameTest {

    private lateinit var root: RootService
    private lateinit var gameConfig: GameConfig

    /**
     * Set up the test environment.
     */
    @BeforeEach
    fun setup() {
        root = RootService()
        gameConfig = GameConfig(
            2.0,
            mutableListOf("John", "Joe"),
            mutableListOf(PlayerType.EASY_BOT, PlayerType.EASY_BOT),
            false,
            false,
            false,
            mutableListOf(GoalTileType.WOOD, GoalTileType.LEAF, GoalTileType.FRUIT),
            mutableListOf(Colors.PURPLE, Colors.BLACK)
        )
    }

    /**
     * Tests the success start Game and validate the game state.
     */
    @Test
    fun `test startGame initialize a full game`() {
        //set up Game State
        val refreshableTest = RefreshableTest()
        root.addRefreshable(refreshableTest)
        root.gameService.startGame(gameConfig)
        assertNotNull(root.game, "Expected game to be in progress")
        val game = requireNotNull(root.game) { "Expected game to be in progress" }.currentState

        //validate cards in the middle
        assertEquals(game.centerCards.size, 4)
        //validate DrawStack
        assertNotEquals(game.drawStack.size, 0)
        //validate Players
        assert(game.players[0].name == "John")
        assert(game.players[1].name == "Joe")
        //player has starting tiles
        assert(game.players[0].storage.size == 1)
        assert(game.players[1].storage.size == 2)
        //player has their chosen color
        assert(game.players[0].color == Colors.PURPLE)
        assert(game.players[1].color == Colors.BLACK)
        //activates refreshable
        assertTrue(refreshableTest.refreshAfterGameStartCalled)

        //has 3 goals if randomized Goals is true
        gameConfig.randomizedGoal = true
        val root1 = RootService()
        root1.gameService.startGame(gameConfig)

        //doesn't crash if randomizedPlayerOrder is true
        gameConfig.randomizedPlayerOrder = true
        val root2 = RootService()
        root2.gameService.startGame(gameConfig)
    }

    /**
     * Tests the extended Version of startGame with a predefined Stack.
     */
    @Test
    fun `test startGame initialize a full game with predefined Stack`() {
        //set up Game State
        //create custom stack
        val storageCardStack = mutableListOf<ToolCard>()
        for (i in 1..28) {
            storageCardStack.add(ToolCard(i, id = 42+i))
        }
        root.gameService.startGame(gameConfig, storageCardStack)
        val game = requireNotNull(root.game) { "Expected game to be in progress" }.currentState


        //validate cards in the middle
        assertEquals(game.centerCards[0] as ToolCard, ToolCard(1, 43))
        assertEquals(game.centerCards[1] as ToolCard, ToolCard(2, id = 44))
        assertEquals(game.centerCards[2] as ToolCard, ToolCard(3, id = 45))
        assertEquals(game.centerCards[3] as ToolCard, ToolCard(4, id = 46))

        //validate Stack
        assertEquals(game.drawStack.size, 24)
    }

    /**
     * Tests the extended Version of startGame with a predefined Stack.
     */
    @Test
    fun `test startGame initialize right drawStacks`() {
        //validate StackSize if just 2 player
        root.gameService.startGame(gameConfig)
        val game = requireNotNull(root.game) { "Expected game to be in progress" }.currentState
        assertEquals(game.drawStack.size, 28)

        //validate StackSize if 3 player
        val root2 =  RootService()
        gameConfig.playerName.add("Jane")
        gameConfig.playerTypes.add(PlayerType.EASY_BOT)
        gameConfig.color.add(Colors.RED)
        root2.gameService.startGame(gameConfig)
        val game2 = requireNotNull(root2.game) { "Expected game to be in progress" }.currentState
        assertEquals(game2.drawStack.size, 39)

        //validate StackSize if 4 player
        val root3 =  RootService()
        gameConfig.playerName.add("Maria")
        gameConfig.playerTypes.add(PlayerType.EASY_BOT)
        gameConfig.color.add(Colors.BLUE)
        root3.gameService.startGame(gameConfig)
        val game3 = requireNotNull(root3.game) { "Expected game to be in progress" }.currentState
        assertEquals(game3.drawStack.size, 43)
    }

    /**
     * Tests the failure of the startGame function of the GameService.
     */
    @Test
    fun `test startGame when preconditions are not met`() {
        //less than 2 players
        val gameConfig1 = GameConfig(
            2.0,
            mutableListOf("John"),
            mutableListOf(PlayerType.EASY_BOT),
            false,
            false,
            false,
            mutableListOf(GoalTileType.WOOD, GoalTileType.LEAF, GoalTileType.FRUIT),
            mutableListOf(Colors.PURPLE)
        )
        assertFailsWith<IllegalArgumentException> { root.gameService.startGame(gameConfig1) }

        //more than 4 players
        val gameConfig2 = GameConfig(
            2.0,
            mutableListOf("John", "Joe", "Jane", "Maria", "Max"),
            mutableListOf(
                PlayerType.EASY_BOT, PlayerType.EASY_BOT, PlayerType.EASY_BOT, PlayerType.EASY_BOT, PlayerType.EASY_BOT
            ),
            false,
            false,
            false,
            mutableListOf(GoalTileType.WOOD, GoalTileType.LEAF, GoalTileType.FRUIT),
            mutableListOf(Colors.PURPLE, Colors.BLACK, Colors.RED, Colors.BLUE, Colors.PURPLE)
        )
        assertFailsWith<IllegalArgumentException> { root.gameService.startGame(gameConfig2) }

        //color is not the same
        val gameConfig3 = GameConfig(
            2.0,
            mutableListOf("John", "Joe"),
            mutableListOf(PlayerType.EASY_BOT, PlayerType.EASY_BOT),
            false,
            false,
            false,
            mutableListOf(GoalTileType.WOOD, GoalTileType.LEAF, GoalTileType.FRUIT),
            mutableListOf(Colors.PURPLE, Colors.PURPLE)
        )
        assertFailsWith<IllegalArgumentException> { root.gameService.startGame(gameConfig3) }

        //player parameters doesn't have the same size
        val gameConfig4 = GameConfig(
            2.0,
            mutableListOf("John", "Joe", "Jane"),
            mutableListOf(PlayerType.EASY_BOT, PlayerType.EASY_BOT),
            false,
            false,
            false,
            mutableListOf(GoalTileType.WOOD, GoalTileType.LEAF, GoalTileType.FRUIT),
            mutableListOf(Colors.PURPLE, Colors.PURPLE)
        )
        assertFailsWith<IllegalArgumentException> { root.gameService.startGame(gameConfig4) }

        //can't start game if it's already running
        //currently there should be no game in root, because every initialization was with an error
        root.gameService.startGame(gameConfig)
        assertFailsWith<IllegalArgumentException> { root.gameService.startGame(gameConfig) }
    }
}