package gui


import entity.Colors
import entity.GoalTileType
import entity.PlayerType
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.core.Color

/**
 * This scene is triggered when the user wants to host an online game. It is triggered from the [ModeScene] and leads to
 * ...
 */
class NetworkHostScene (private val rootService: RootService):
    MenuScene(1920, 1080, background = ImageVisual("Ground.jpg")), Refreshable {
    private val selectedGoalTypes = mutableListOf<GoalTileType>()
    private val playerNames by lazy { mutableListOf(playerName) }
    private val playerColors by lazy { mutableListOf(playerColor) }
    private val usedColors = mutableSetOf<Colors>()


    private val contentPane = Pane<UIComponent>(
        width = 800,
        height = 900,
        posX = 1920 / 2 - 800 / 2,
        posY = 1080 / 2 - 900 / 2,
        visual = ColorVisual(Color(255, 255, 255, 100))
    )

    private val titleLabel = Label(
        text = "Host Game",
        width = 700,
        height = 100,
        posX = 55,
        posY = 30,
        alignment = Alignment.CENTER,
        font = Font(40,Color(0x000000), "JetBrains Mono ExtraBold")
    )

    private val playerName = TextField(
        prompt = "Name",
        width = 200,
        height = 75,
        posX = 25,
        posY = 150,
        font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D)),
    )

    private val playerRoleInput: ComboBox<PlayerType> = ComboBox<PlayerType>(
        width = 200,
        height = 75,
        posX = 250,
        posY = 150,
        prompt = "select Role",
        font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xFFFFFF))
    ).apply {
        items = PlayerType.entries.filterNot { it == PlayerType.ONLINE }
        selectedItem = PlayerType.LOCAL
        formatFunction = {
            when (it) {
                PlayerType.EASY_BOT -> "Easy Bot"
                PlayerType.HARD_BOT -> "Hard Bot"
                PlayerType.LOCAL -> "Human"
                else -> error("No valid player type!")
            }
        }
        opacity = 0.5
    }

    private val playerColor: ComboBox<Colors> = ComboBox<Colors>(
        width = 200,
        height = 75,
        posX = 480,
        posY = 150,
        font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xFFFFFF))
    ).apply {
        isVisible = true
        items = Colors.entries
        selectedItem = Colors.RED
        usedColors.add(selectedItem!!)
        formatFunction = {
            when (it) {
                Colors.RED -> "Red"
                Colors.BLUE -> "Blue"
                Colors.BLACK -> "Black"
                Colors.PURPLE -> "Purple"
            }
        }
        opacity = 0.5
    }

    val backToLastSceneButton = Button(
        text = "BACK",
        width = 280,
        height = 60,
        posX = 50,
        posY = 790,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    )

    private val chooseGoalButton = Button(
        text = "Continue",
        width = 280,
        height = 60,
        posX = 470,
        posY = 790,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        isVisible = true
        onMouseClicked = {
            chooseGoals()
        }

    }

    private val backFromGoalSelectionButton = Button(
        text = "BACK",
        width = 280,
        height = 60,
        posX = 50,
        posY = 790,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        isVisible = false // Initially hidden
        onMouseClicked = { restorePlayerSetupUI() }
    }

    private val hostButton = Button(
        text = "HOST",
        width = 280,
        height = 60,
        posX = 1920 / 2 - 800 / 2 - 300,
        posY = 1080 / 2 - 900 / 2 + 600,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        isVisible = false

        onMouseClicked = {
            if (selectedGoalTypes.size == 3){
                rootService.networkService.hostGame(playerName.text,
                    requireNotNull(playerRoleInput.selectedItem)
                )
                displayHostedScreen(playerName.text,
                    requireNotNull(playerColor.selectedItem),
                    requireNotNull(playerRoleInput.selectedItem))
            }
            else{
                println("ERROR: Please select goal tiles.")
            }
        }
    }

    private val startButton = Button(
        text = "Start",
        width = 280,
        height = 60,
        posX = 1920 / 2 - 800 / 2 - 300,
        posY = 1080 / 2 - 900 / 2 + 600,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        isVisible = false

        onMouseClicked = {
            try {
                rootService.networkService.createHostedGame(
                    playerColors.mapNotNull { it.selectedItem }.toMutableList(),
                    selectedGoalTypes
                )
            } catch (e: Exception){
                println("ERROR: startGame() crashed - ${e.message}")
            }
        }
    }

    private val startButtonRandom = Button(
        text = "Randomized Goals",
        width = 280,
        height = 60,
        posX = 470,
        posY = 790,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        isVisible = true

        onMouseClicked = {
            rootService.networkService.createHostedGame(
                playerColors.mapNotNull { it.selectedItem }.toMutableList(),
                selectedGoalTypes,
                true
            )
        }
    }

    private val idLabel = Label(
        text = "test",
        width = 280,
        height = 60,
        posX = 1920 / 2 - 800 / 2 - 300,
        posY = 1080 / 2 - 900 / 2 + 400,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply { isVisible=false }

    init {
        contentPane.addAll(
            hostButton,
            chooseGoalButton,
            backToLastSceneButton,
            playerName,
            playerRoleInput,
            idLabel,
            playerColor
        )
        addComponents(contentPane)
    }

    private fun createGoalButton(imageName: String, goalType: GoalTileType, offsetX: Int, offsetY: Int): Button {
        return Button(
            posX = 1920 / 2 - 800 / 2 + offsetX,
            posY = 1080 / 2 - 900 / 2 + offsetY,
            width = 180,
            height = 120,
            visual = ImageVisual(imageName),
            font = Font(0)
        ).apply {
            text = goalType.name // Store goal type as text for easy reference
        }
    }

    private fun chooseGoals() {
        // Hide irrelevant UI elements
        listOf(
            chooseGoalButton,
            backToLastSceneButton,
            playerRoleInput,
        ).forEach { it.isVisible = false }

        selectedGoalTypes.clear()

        val goalButtons = listOf(
            createGoalButton("LeafGoal1.jpg", GoalTileType.LEAF, -480, 80),
            createGoalButton("WoodGoal1.jpg", GoalTileType.WOOD, 0, 80),
            createGoalButton("FlowerGoal1.jpg", GoalTileType.FLOWER, -480, 320),
            createGoalButton("FruitGoal1.jpg", GoalTileType.FRUIT, 0, 320),
            createGoalButton("PositionGoal1.jpg", GoalTileType.POSITION, -240, 200)
        )

        goalButtons.forEach { button ->
            button.onMouseClicked = {
                val goalType = GoalTileType.valueOf(button.text)

                if (selectedGoalTypes.contains(goalType)) {
                    selectedGoalTypes.remove(goalType)
                    button.opacity = 1.0
                } else if (selectedGoalTypes.size < 3) {
                    selectedGoalTypes.add(goalType)
                    button.opacity = 0.2
                }
                hostButton.isDisabled = selectedGoalTypes.size != 3
            }
        }

        backFromGoalSelectionButton.isVisible = true
        hostButton.apply {
            isVisible = true
            isDisabled = true  // Disabled until 3 goals are selected
        }

        // Update UI components
        contentPane.clear()
        contentPane.addAll(goalButtons + hostButton + backFromGoalSelectionButton + startButtonRandom)
    }

    private fun restorePlayerSetupUI() {
        // Restore all UI elements
        listOf(
            chooseGoalButton,
            backToLastSceneButton,
            playerRoleInput,
            playerName,
            hostButton,
            playerColor
        ).forEach { it.isVisible = true }

        // Hide goal selection UI
        backFromGoalSelectionButton.isVisible = false
        hostButton.isVisible = false
        contentPane.clear()
        contentPane.addAll(
            titleLabel, chooseGoalButton, backToLastSceneButton, playerName,
            playerRoleInput, hostButton
        )
    }

    private fun displayHostedScreen(
        name: String, color: Colors, role: PlayerType
    ){
        val id = requireNotNull(rootService.networkService.client?.sessionID)
        idLabel.text = id
        restorePlayerSetupUI()
        playerRoleInput.selectedItem = role
        playerColor.selectedItem = color
        playerName.text = name


        listOf(
            hostButton,
            chooseGoalButton,
            playerRoleInput,
            backToLastSceneButton,
            backFromGoalSelectionButton
        ).forEach { it.isVisible = false }

        listOf(
            startButton,
            idLabel,
            playerName,
            playerRoleInput,
            playerColor
        ).forEach { it.isVisible = true }
        contentPane.clear()
        contentPane.addAll(
            startButton, idLabel, playerName
        )
    }

    override fun refreshAfterJoin(name: String) {
        if (playerNames.size >= 4) return

        // Get the current index of the player input field to be created
        val newIndex = playerNames.size

        // Create a new player input field
        val newPlayerInput = TextField(
            text = name,
            width = 200,
            height = 75,
            posX = 25,
            posY = 150 + 100 * newIndex,
            font = Font(26,Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
            visual = ColorVisual(Color(0x49585D)),
        )

        val newPlayerColor: ComboBox<Colors> = ComboBox<Colors>(
            width = 200,
            height = 75,
            posX = 480,
            posY = 150 + 100 * newIndex,
            font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
            visual = ColorVisual(Color(0xFFFFFF))
        ).apply {
            items = Colors.entries.filterNot { it in usedColors }
            selectedItem = Colors.entries.filterNot { it in usedColors }.random()
            usedColors.add(selectedItem!!)
            formatFunction = {
                when (it) {
                    Colors.RED -> "Red"
                    Colors.BLUE -> "Blue"
                    Colors.BLACK -> "Black"
                    Colors.PURPLE -> "Purple"
                }
            }
            opacity = 0.5
        }

        // Add new components to the content pane
        contentPane.add(newPlayerInput)
        contentPane.add(newPlayerColor)

        // Add new components to the lists
        playerNames.add(newPlayerInput)
        playerColors.add(newPlayerColor)
    }
}
