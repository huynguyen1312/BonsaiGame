package gui

import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.core.Color

/**
 * In this scene the game's settings can be configured.  It is reached from the [MainMenuScene] and leads back to the
 * [MainMenuScene]
 */
class OptionMenuScene(private val rootService: RootService):
    MenuScene(1920, 1080, background = ImageVisual("Ground.jpg")), Refreshable {

    var botSpeed = 0.0

    private val contentPane = Pane<UIComponent>(
        width = 700,
        height = 900,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2,
        visual = ColorVisual(Color(0x0C2027))
    ).apply { opacity = 0.65 }


    private val titleLabel = Label(
        text = "Setting",
        width = 700,
        height = 100,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2,
        alignment = Alignment.CENTER,
        font = Font(40, Color(0xFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val botSpeedLabel = Label(
        width = 200,
        height = 35,
        posX = 1920 / 2 - 700 / 2,
        posY = 1080 / 2 - 900 / 2 + 350,
        text = "Bots speed (s): ",
        font = Font(25, Color(0xFFFFFF), "JetBrains Mono ExtraBold")
    )

    private val botSpeedInput: ComboBox<Double> = ComboBox<Double>(
        width = 450,
        height = 50,
        posX = 1920 / 2 - 700 / 2 + 200,
        posY = 1080 / 2 - 900 / 2 + 342,
        prompt = "select item",
        font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xFFFFFF))
    ).apply {
        items = listOf(2.0,5.0,10.0)
        selectedItem = 2.0
        formatFunction = {
            when(it) {
                2.0 -> "2.0"
                5.0 -> "5.0"
                10.0 -> "10.0"
                else -> error("no valid bot speed selected!")
            }
        }
        onItemSelected = { newValue ->
            when(newValue) {
                2.0 -> botSpeed = 2.0
                5.0 -> botSpeed = 5.0
                10.0 -> botSpeed = 10.0
                else -> error("no valid bot speed selected!")
            }
        }
        opacity = 0.5
    }

    val backToMenuButton = Button(
        text = "Back",
        width = 200,
        height = 100,
        posX = 1920 / 2 - 100,
        posY = 1080 / 2 - 900 / 2 + 700,
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    )

    init {
        addComponents(
            contentPane,
            titleLabel,
            botSpeedLabel,
            botSpeedInput,
            backToMenuButton
        )
    }
}