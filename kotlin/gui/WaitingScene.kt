package gui

import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.core.Color

/**
 * this scene is a Waiting screen that is triggered when wanting to join an online game after the configuration in
 * [NetworkJoinScene] is done
 */
class WaitingScene(private val rootService: RootService):
    MenuScene(1920,1080, background = ImageVisual("Ground.jpg")), Refreshable {

    private val contentPane = Pane<UIComponent>(
        width = 700,
        height = 900,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2,
        visual = ColorVisual(Color(0x0C2027))
    ).apply { opacity = 0.65 }

    private val titleLabel = Label(
        text = "Waiting for Host",
        width = 700,
        height = 100,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2,
        alignment = Alignment.CENTER,
        font = Font(40, Color(0xFFFFFF), "JetBrains Mono ExtraBold")
    )
    init {
        addComponents(
            titleLabel,
            contentPane
        )
    }
    private val networkJoinScene = NetworkJoinScene(rootService)

    override fun refreshAfterJoin(name: String){
        networkJoinScene.playerInput.text = name
//        rootService.gameService.startGame(gameConfig = GameConfig(), drawStack = mutableListOf<ZenCard>())
    }
}