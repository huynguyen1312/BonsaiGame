package gui

import entity.Colors
import entity.GoalTileType
import entity.PlayerType
import service.GameConfig
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import kotlin.system.exitProcess

/**
 * The Application GUI's main file. From here a game with its GUI is started
 */
class BonsaiGameApplication : BoardGameApplication("Bonsai"), Refreshable {

    private val rootService = RootService()

    private var gameScene: GameScene
    private lateinit var mainMenuScene: MainMenuScene
    private lateinit var modeScene: ModeScene
    private var resultMenuScene: ResultMenuScene
    private lateinit var hotSeatScene: HotSeatScene
    private lateinit var networkJoinScene: NetworkJoinScene
    private lateinit var networkHostScene: NetworkHostScene
    private lateinit var optionMenuScene: OptionMenuScene
    private var lobbyScene: LobbyScene
    private var waitingScene: WaitingScene

    init {
        gameScene = GameScene(rootService).apply {
            saveGameButton.onMouseClicked = {
                rootService.fileService.saveGame()
                this@BonsaiGameApplication.showMenuScene(mainMenuScene)
            }
        }

        mainMenuScene = MainMenuScene(rootService).apply {
            newGameButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(modeScene)
            }
            settingsButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(optionMenuScene)
            }
            exitButton.onMouseClicked = {
                exitProcess(0)
            }
            continueGameButton.onMouseClicked = {
                rootService.fileService.loadGame()
            }
        }

        modeScene = ModeScene(rootService).apply {
            hotSeatButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(hotSeatScene)
            }
            joinButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(networkJoinScene)
                ConnectionRole.GUEST
            }
            hostButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(networkHostScene)
                ConnectionRole.HOST
            }
            backToMenuButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(mainMenuScene)
            }
        }

        resultMenuScene = ResultMenuScene(rootService).apply {
            backToMenuButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(mainMenuScene)
            }
            exitButton.onMouseClicked = {
                exitProcess(0)
            }
        }

        val config = GameConfig(
            1.0,
            mutableListOf("HANS", "PETER"),
            mutableListOf(PlayerType.LOCAL, PlayerType.LOCAL),
            true,
            true,
            false,
            mutableListOf(GoalTileType.LEAF, GoalTileType.FLOWER, GoalTileType.WOOD),
            mutableListOf(Colors.RED, Colors.BLACK)
        )


        hotSeatScene = HotSeatScene(rootService).apply {
            backToLastSceneButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(modeScene)
            }
        }

        networkJoinScene = NetworkJoinScene(rootService).apply {
            backButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(modeScene)
            }
        }

        networkHostScene = NetworkHostScene(rootService).apply {
            backToLastSceneButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(modeScene)
            }
        }

        optionMenuScene = OptionMenuScene(rootService).apply {
            backToMenuButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(mainMenuScene)
            }
        }

        lobbyScene = LobbyScene(rootService).apply {
            backToLastSceneButton.onMouseClicked = {
                this@BonsaiGameApplication.showMenuScene(networkJoinScene)
            }
            startButton.onMouseClicked = {
                this@BonsaiGameApplication.showGameScene(gameScene)
            }
        }

        waitingScene = WaitingScene(rootService)

        rootService.addRefreshables(
            this,
            gameScene,
            resultMenuScene,
            mainMenuScene,
            modeScene,
            optionMenuScene,
            networkJoinScene,
            networkHostScene,
            lobbyScene,
            hotSeatScene,
            waitingScene,
            ConsoleRefreshable(rootService)
        )
//        this.showMenuScene(mainMenuScene)
        rootService.gameService.startGame(config)
    }

    override fun refreshAfterGameStart() {
        this.hideMenuScene()
        println("refreshAfterGameStart() called!")
        this.showGameScene(gameScene)
    }

    override fun refreshAfterEndGame(scores: Array<IntArray>, winner: String) {
        this.showMenuScene(resultMenuScene)
    }

    override fun refreshAfterLoadGame() {
        refreshAfterGameStart()
    }
}
