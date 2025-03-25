package gui

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.core.Color

/**
 * This is the main menu. The first scene shown when a game is started
 */
class MainMenuScene(private val rootService: RootService):
    MenuScene(1920, 1080, background = ImageVisual("Ground.png")), Refreshable {

    val newGameButton = Button(
        text = "New Game",
        width = 300,
        height = 80,
        posX = 120,
        posY = 800,
        font = Font(30, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    )

    val settingsButton = Button(
        text = "Settings",
        width = 300,
        height = 80,
        posX = 1520,
        posY = 800,
        font = Font(30, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    )

    val exitButton = Button(
        text = "Quit Game",
        width = 300,
        height = 80,
        posX = 1520,
        posY = 950,
        font = Font(30, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    )

    val continueGameButton = Button(
        text = "Resume",
        width = 300,
        height = 80,
        posX = 120,
        posY = 950,
        font = Font(30, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    )

    init {
        addComponents(
            newGameButton,
            settingsButton,
            exitButton,
            continueGameButton
        )
        opacity = 0.3
    }

}
