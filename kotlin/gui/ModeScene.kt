package gui

import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.core.Color

/**
 * In this scene the kind of game that is to be played needs to be chosen. The player can decide between creating a
 * hot seat game, joining an online game and hosting an online game.
 */
class ModeScene(private val rootService: RootService):
    MenuScene(1920, 1080, background = ImageVisual("Ground.jpg")), Refreshable {


    private val contentPane = Pane<UIComponent>(
        width = 700,
        height = 900,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2,
        visual = ColorVisual(Color(255, 255, 255, 100))
    ).apply { opacity = 0.65 }

    private val titleLabel = Label(
        text = "Game Mode",
        width = 700,
        height = 100,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2,
        alignment = Alignment.CENTER,
        font = Font(40, Color(0x000000), "JetBrains Mono ExtraBold")
    )

    val hostButton = Button(
        text = "Host Online Game",
        width = 400,
        height = 100,
        posX = 1920 / 2 - 700 / 2 + 150,
        posY = 1080 / 2 - 900 / 2 + 150,
        font = Font(26, Color(0xFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    )

    val joinButton = Button(
        text = "Join Online Game",
        width = 400,
        height = 100,
        posX = 1920 / 2 - 700 / 2 + 150,
        posY = 1080 / 2 - 900 / 2 + 350,
        font = Font(26, Color(0xFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    )

    val hotSeatButton = Button(
        text = "Hot Seat",
        width = 400,
        height = 100,
        posX = 1920 / 2 - 700 / 2 + 150,
        posY = 1080 / 2 - 900 / 2 + 550,
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    )

    val backToMenuButton = Button(
        text = "Back",
        width = 200,
        height = 100,
        posX = 1920 / 2 - 100,
        posY = 1080 / 2 - 900 / 2 + 750,
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    )

    init {
        // Add all components to the scene
        addComponents(
            contentPane,
            titleLabel,
            hostButton,
            joinButton,
            hotSeatButton,
            backToMenuButton
        )
        opacity = 0.3
    }

}