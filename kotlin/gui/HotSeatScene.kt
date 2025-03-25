package gui

import entity.Colors
import entity.GoalTileType
import entity.PlayerType
import service.GameConfig
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual

/**
 * The scene that is shown when wanting to create a hot seat scene. Here the players are configured. Afterward the
 * goals are chosen.
 */
class HotSeatScene(private val rootService: RootService) :
    MenuScene(1920, 1080, background = ImageVisual("Ground.jpg")), Refreshable {

    private val playerNameInputs by lazy { mutableListOf(playerNameDefaultInput) }
    private val playerRemoves by lazy { mutableListOf(playerRemove) }
    private val playerRoles by lazy { mutableListOf(playerRoleInput) }
    private val playerColors by lazy { mutableListOf(playerColor) }
    private val usedColors = mutableSetOf<Colors>()


    private val selectedGoalTypes = mutableListOf<GoalTileType>()
    private var randomizedOrder = false


    private val contentPane = Pane<UIComponent>(
        width = 800,
        height = 900,
        posX = 1920 / 2 - 800 / 2,
        posY = 1080 / 2 - 900 / 2,
        visual = ColorVisual(Color(255, 255, 255, 100))
    )

    private val titleLabel = Label(
        text = "Hot Seat Setup",
        width = 700,
        height = 100,
        posX = 55,
        posY = 30,
        alignment = Alignment.CENTER,
        font = Font(40, Color(0x000000), "JetBrains Mono ExtraBold")
    )

    private val playerNameDefaultInput = TextField(
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
                PlayerType.LOCAL -> "Local"
                else -> error("No valid player type!")
            }
        }
        opacity = 0.5
    }

    private val playerRemove = Button(
        width = 75,
        height = 75,
        posX = 700,
        posY = 150,
        font = Font(35, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = CompoundVisual(
            ColorVisual(Color(0x49585D)), ImageVisual("remove.png")
        )
    ).apply {
        // When the button is clicked, the first player is removed
        onMouseClicked = {
            removePlayer(0)
        }
    }

    private val playerColor: ComboBox<Colors> = ComboBox<Colors>(
        width = 200,
        height = 75,
        posX = 480,
        posY = 150,
        font = Font(25, Color(0x000000), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xFFFFFF))
    ).apply {
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

    private val playerAdd = Button(
        width = 75,
        height = 75,
        posX = 700 / 2,
        posY = 275,
        font = Font(35, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = CompoundVisual(
            ColorVisual(Color(0x49585D)), ImageVisual("add.png")
        )
    ).apply {
        // When the button is clicked, a new player is added
        onMouseClicked = {
            addPlayer()
        }
    }

    private val randomizeOrderButton = Button(
        text = "RANDOMIZE ORDER",
        width = 280,
        height = 60,
        posX = 250,
        posY = 700,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        onMouseClicked = {
            randomizedOrder = !randomizedOrder
            text = if (randomizedOrder) "UNRANDOMIZE" else "RANDOMIZE ORDER"
        }
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

    private val startButton = Button(
        text = "START",
        width = 280,
        height = 60,
        posX = 1920 / 2 - 800 / 2 - 300,
        posY = 1080 / 2 - 900 / 2 + 600,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        isVisible = false // Initially hidden until goal selection

        onMouseClicked = {
            println("Start button clicked!")
            val playerNames =
                playerNameInputs.filter { it.text.isNotBlank() && it.text.length <= 20 }.map { it.text }.toMutableList()
            val playerTypes = playerRoles.mapNotNull { it.selectedItem }.toMutableList()
            val playerColors = playerColors.mapNotNull { it.selectedItem }.toMutableList()

            println("Player Names: $playerNames")
            println("Player Types: $playerTypes")
            println("Player Colors: $playerColors")
            println("Selected Goals: $selectedGoalTypes")

            // Ensure 3 goals are selected
            if (selectedGoalTypes.size != 3) {
                println("ERROR: Exactly 3 goals must be selected before starting the game.")
                throw IllegalArgumentException("ERROR: Exactly 3 goals must be selected before starting the game.")
            }

            // Ensure 2-4 players are present
            if (playerNames.size < 2 || playerNames.size > 4) {
                println("ERROR: The game must have between 2 and 4 players.")
                throw IllegalArgumentException("ERROR: The game must have between 2 and 4 players.")
            }




            // Create the game configuration
            val gameConfig = GameConfig(
                botSpeed = 5.0, // Default bot speed
                playerName = playerNames,
                playerTypes = playerTypes,
                hotSeat = true,
                randomizedGoal = false, //TODO what button displays
                randomizedPlayerOrder = randomizedOrder,
                goalTiles = selectedGoalTypes.toMutableList(),
                color = playerColors
            )
            println("GameConfig created: $gameConfig")
            // Start the game
            try {
                rootService.gameService.startGame(gameConfig)
                println("startGame() call completed")
            } catch (e: Exception) {
                println("ERROR: startGame() crashed - ${e.message}")
            }
        }
    }


    private val startButtonRandom = Button(
        text = "RANDOMIZED GOALS",
        width = 280,
        height = 60,
        posX = 470,
        posY = 790,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x49585D))
    ).apply {
        isVisible = false // Initially hidden until goal selection

        onMouseClicked = {
            println("Start button clicked!")
            val playerNames =
                playerNameInputs.filter { it.text.isNotBlank() && it.text.length <= 20 }.map { it.text }.toMutableList()
            val playerTypes = playerRoles.mapNotNull { it.selectedItem }.toMutableList()
            val playerColors = playerColors.mapNotNull { it.selectedItem }.toMutableList()

            println("Player Names: $playerNames")
            println("Player Types: $playerTypes")
            println("Player Colors: $playerColors")
            //println("Selected Goals: $randomizedGoalTypes")

            // Ensure 2-4 players are present
            if (playerNames.size < 2 || playerNames.size > 4) {
                println("ERROR: The game must have between 2 and 4 players.")
                throw IllegalArgumentException("ERROR: The game must have between 2 and 4 players.")
            }

            // Create the game configuration
            val gameConfig = GameConfig(
                botSpeed = 5.0, // Default bot speed
                playerName = playerNames,
                playerTypes = playerTypes,
                hotSeat = true,
                randomizedGoal = true, //TODO what button displays
                randomizedPlayerOrder = randomizedOrder,
                //goalTiles = randomizedGoalTypes.toMutableList(),
                color = playerColors
            )
            println("GameConfig created: $gameConfig")
            // Start the game
            try {
                rootService.gameService.startGame(gameConfig)
                println("startGame() call completed")
            } catch (e: Exception) {
                println("ERROR: startGame() crashed - ${e.message}")
            }

        }
    }

    // Initialize the scene by setting the background color and adding all components to the content pane
    init {
        contentPane.addAll(
            titleLabel,
            playerNameDefaultInput,
            playerRoleInput,
            playerRemove,
            playerColor,
            playerAdd,
            randomizeOrderButton,
            backToLastSceneButton,
            chooseGoalButton,
            startButton,
            startButtonRandom
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

    /**
     * Opens the goal selection UI, allowing players to choose exactly three goal tiles.
     */
    private fun chooseGoals() {
        // Hide irrelevant UI elements
        listOf(
            chooseGoalButton,
            playerNameDefaultInput,
            playerRemove,
            playerAdd,
            startButton,
            backToLastSceneButton,
            randomizeOrderButton,
            playerRoleInput,
            playerColor
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
                startButton.isDisabled = selectedGoalTypes.size != 3
            }
        }

        backFromGoalSelectionButton.isVisible = true
        startButton.apply {
            isVisible = true
            isDisabled = true  // Disabled until 3 goals are selected
        }
        startButtonRandom.isVisible = true

        // Update UI components
        contentPane.clear()
        contentPane.addAll(goalButtons + startButton + backFromGoalSelectionButton + startButtonRandom)
    }



    /**
     * Restores the player setup UI after selecting goals.
     */
    private fun restorePlayerSetupUI() {
        // Restore all UI elements
        listOf(
            chooseGoalButton,
            playerNameDefaultInput,
            playerRemove,
            playerAdd,
            startButton,
            startButtonRandom,
            backToLastSceneButton,
            randomizeOrderButton,
            playerRoleInput,
            playerColor
        ).forEach { it.isVisible = true }

        // Hide goal selection UI
        backFromGoalSelectionButton.isVisible = false
        startButton.isVisible = false
        startButtonRandom.isVisible = false
        contentPane.clear()
        contentPane.addAll(
            titleLabel, chooseGoalButton, backToLastSceneButton, playerAdd, randomizeOrderButton
        )

        for (i in playerNameInputs.indices) {
            contentPane.add(playerNameInputs[i])
            contentPane.add(playerRoles[i])
            contentPane.add(playerColors[i])
            contentPane.add(playerRemoves[i])
        }
    }

    /**
     * Adds a new player to the configuration scene, allowing up to four players.
     */
    private fun addPlayer() {
        // Disallow adding more than four players
        if (playerNameInputs.size >= 4) return

        // Get the current index of the player input field to be created
        val newIndex = playerNameInputs.size

        // Create a new player input field
        val newPlayerInput = TextField(
            prompt = "Name",
            width = 200,
            height = 75,
            posX = 25,
            posY = 150 + 100 * newIndex,
            font = Font(26, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
            visual = ColorVisual(Color(0x49585D)),
        )

        val newPlayerRoleInput = ComboBox<PlayerType>(
            width = 200,
            height = 75,
            posX = 250,
            posY = 150 + 100 * newIndex,
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
                    PlayerType.LOCAL -> "Local"
                    else -> error("No valid player type!")
                }
            }
            opacity = 0.5
        }

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

        // Create a new remove button for the player input field
        val newPlayerRemove = Button(
            width = 75,
            height = 75,
            posX = 700,
            posY = 150 + 100 * newIndex,
            font = Font(35, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
            visual = CompoundVisual(
                ColorVisual(Color(0x49585D)), ImageVisual("remove.png")
            )
        ).apply {
            // When the button is clicked, the player input field is removed
            onMouseClicked = {
                removePlayer(newIndex)
            }
        }

        // Add new components to the content pane
        contentPane.add(newPlayerInput)
        contentPane.add(newPlayerRemove)
        contentPane.add(newPlayerRoleInput)
        contentPane.add(newPlayerColor)

        // Add new components to the lists
        playerNameInputs.add(newPlayerInput)
        playerRemoves.add(newPlayerRemove)
        playerRoles.add(newPlayerRoleInput)
        playerColors.add(newPlayerColor)
        // Move the add player button down by 100 pixels
        playerAdd.posY += 100
    }

    /**
     * Removes a player from the setup screen, adjusting all UI components accordingly.
     *
     * @param index The index of the player to remove.
     */
    private fun removePlayer(index: Int) {
        // Disallow removing the last player remaining
        if (playerNameInputs.size > 1) {

            usedColors.remove(playerColors[index].selectedItem)

            // Remove UI components from scene
            contentPane.remove(playerNameInputs[index])
            contentPane.remove(playerRoles[index])
            contentPane.remove(playerColors[index])
            contentPane.remove(playerRemoves[index])

            // Remove elements from the lists
            playerNameInputs.removeAt(index)
            playerRoles.removeAt(index)
            playerColors.removeAt(index)
            playerRemoves.removeAt(index)


            // Iterate over all player input fields and remove buttons after the removed index
            // Move them up by 100 pixels to fill the gap
            // Update the on click event of the remove buttons to remove the correct player
            for (i in index until playerNameInputs.size) {
                playerNameInputs[i].posY -= 100
                playerRoles[i].posY -= 100
                playerColors[i].posY -= 100
                playerRemoves[i].posY -= 100

                // Rebind remove button event to the correct index
                playerRemoves[i].onMouseClicked = {
                    removePlayer(i)
                }
            }

            // Move the add player button up by 100 pixels
            playerAdd.posY -= 100
        }
    }

}
