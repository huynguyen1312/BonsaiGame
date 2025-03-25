package service

import entity.*
import service.bot.BotMemory
import service.bot.BotPhase

/**
 * Handles bot actions and decision-making during the game.
 *
 * This service is responsible for executing moves for bot players, ensuring they follow
 * game rules and make valid decisions based on the current game state. Moves are generated
 * randomly among valid options.
 *
 * @property root A reference to the main [RootService], which grants access to other services
 *                (such as [PlayerActionService] and [TreeService]) and the running game.
 */
open class BotService(
    val root: RootService
) : AbstractRefreshingService() {

    /**
     * Shorthand to retrieve the current [BonsaiGame] from the [RootService].
     * @throws IllegalStateException if no game is currently running.
     */
    private val game: BonsaiGame
        get() = checkNotNull(root.game) { "No Game is currently running" }

    /**
     * Shorthand to retrieve the current [GameState] from the [BonsaiGame].
     * @throws IllegalStateException if there is no active game state.
     */
    private val currentState: GameState
        get() = checkNotNull(game.currentState) { "There isn't a current game running" }

    /**
     * A reference to the [PlayerActionService], used for high-level game actions
     * like cultivate, meditate, discard, etc.
     */
    private val playerActionS: PlayerActionService
        get() = root.playerActionService

    /**
     * A reference to the [TreeService], which handles tile placement logic
     * (e.g., adjacency rules, hex coordinates, etc.).
     */
    private val treeS: TreeService
        get() = root.treeService

    private val botMemory = BotMemory()
    private var currentPhase: BotPhase = BotPhase.EARLY

    /**
     * Executes the bot's turn. This method checks if the current player is actually a bot,
     * then repeatedly calls [makeRandomBotMove] until the bot's turn is finished or the game ends.
     *
     * ## Preconditions:
     * - A game must be active.
     * - The current player must be a bot ([PlayerType.EASY_BOT] or [PlayerType.HARD_BOT]).
     *
     * ## Postconditions:
     * - The bot performs random valid moves until it either ends its turn or the game ends.
     *
     * @throws IllegalStateException If no active game exists or if the current player is not a bot.
     */
    open fun makeBotMove() {
        val bot = currentState.currentPlayer
        require(bot.type == PlayerType.EASY_BOT || bot.type == PlayerType.HARD_BOT) {
            "Player must be a bot to make a bot move"
        }

        println("🤖 ${currentState.currentPlayer.name} is starting its turn")
        while (root.game != null && currentState.currentPlayer === bot
            && currentState.state != States.TURN_END && currentState.state != States.GAME_ENDED) {
            println("🤖 Bot is making a move in state: ${currentState.state}")

            if (game.currentState.lastRound && (game.currentState.players[game.currentState.finalPlayer] == bot)) {
                Thread.sleep((game.botSpeed * 200).toLong())
                if (bot.type == PlayerType.EASY_BOT) {
                    makeRandomBotMove()
                } else makeRealBotMove()
                game.currentState.state = States.GAME_ENDED
            } else {
                Thread.sleep((game.botSpeed * 200).toLong())
                if (bot.type == PlayerType.EASY_BOT) {
                    makeRandomBotMove()
                } else makeRealBotMove()
            }
        }
    }


    private fun tilePriority(type: BonsaiTileType): Int {
        return when (type) {
            BonsaiTileType.WOOD -> 1
            BonsaiTileType.LEAF -> 2
            BonsaiTileType.FRUIT -> 3
            BonsaiTileType.FLOWER -> 4
            BonsaiTileType.ANY -> 500
        }
    }

    private fun updateBotMemory() {
        botMemory.deckSize = currentState.drawStack.size

        botMemory.remainingCards.removeAll(currentState.centerCards.filterNotNull().toSet())
    }

    private fun makeRealBotMove() {
        if (root.game == null || game.currentState.state == States.GAME_ENDED) {
            println("🚨 Game has ended. Bot cannot make a move.")
            return
        }

        val bot = currentState.currentPlayer

        require(bot.type == PlayerType.HARD_BOT) {
            "Player must be a real bot to make a grown up bot move"
        }
        updateBotMemory()

        while (root.game != null && currentState.currentPlayer == bot) {
            when (currentState.state) {

                States.CHOOSE_ACTION -> {
                    val tileCount = bot.storage.size

                    val doMeditate = when {
                        tileCount <= 3 -> true
                        else -> false
                    }

                    if (doMeditate) {
                        playerActionS.meditate(pickBestMeditateCard())
                    } else {
                        playerActionS.cultivate()
                    }

                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.CULTIVATE -> {
                    var possiblePositions = treeS.getPossiblePlacements()
                    val sortedTiles = bot.storage.sortedBy { tilePriority(it.type) }.toMutableList()
                    println("$sortedTiles")
                    for (tile in sortedTiles.toList()) {
                        val validPositions = possiblePositions.filter {
                            treeS.canPlaceTile(tile.type, it)
                        }

                        if (sortedTiles.isNotEmpty() && validPositions.isNotEmpty()) {
                            val chosenPos = validPositions.random()
                            root.treeService.placeTile(tile, chosenPos)
                            sortedTiles.remove(tile)
                            possiblePositions = root.treeService.getPossiblePlacements()
                        }
                    }

                    if (calculateReachedGoals().isNotEmpty()) {
                        game.currentState.state = States.CLAIMING_GOALS
                    } else {
                        game.currentState.state = States.TURN_END
                    }

                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.CHOOSING_2ND_PLACE_TILES -> {
                    val woodCount = bot.storage.count { it.type == BonsaiTileType.WOOD }
                    val leafCount = bot.storage.count { it.type == BonsaiTileType.LEAF }

                    if (woodCount <= 2) {
                        playerActionS.chooseTile(BonsaiTileType.WOOD)
                    } else if (leafCount <= 2) {
                        playerActionS.chooseTile(BonsaiTileType.LEAF)

                        Thread.sleep((game.botSpeed * 200).toLong())
                    }
                }

                States.USING_MASTER -> {
                    val woodCount = bot.storage.count { it.type == BonsaiTileType.WOOD }
                    val leafCount = bot.storage.count { it.type == BonsaiTileType.LEAF }
                    val fruitCount = bot.storage.count { it.type == BonsaiTileType.FRUIT }
                    val flowerCount = bot.storage.count { it.type == BonsaiTileType.FLOWER }
                    var chosenTile: BonsaiTileType

                    if (woodCount == 0) {
                        chosenTile = BonsaiTileType.WOOD
                    } else if (leafCount < 2) {
                        chosenTile = BonsaiTileType.LEAF
                    } else if (fruitCount == 0 && flowerCount == 0) {
                        chosenTile = BonsaiTileType.FRUIT
                    } else {
                        chosenTile = BonsaiTileType.FLOWER
                    }
                    game.currentState.currentPlayer.storage.add(BonsaiTile(chosenTile))
                    game.currentState.state = States.TURN_END
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.USING_HELPER -> {
                    var possiblePositions = treeS.getPossiblePlacements()
                    val game = checkNotNull(root.game)
                    val helperCard = checkNotNull(currentState.currentPlayer.discardPile.last())
                    val typesInCard = (helperCard as HelperCard).tiles.toMutableList()
                    val tilesInStorage = bot.storage.toMutableList()
                    val tilesInCard = mutableListOf<BonsaiTile>()

                    for (tile in typesInCard) {
                        if (tile == BonsaiTileType.ANY) {
                            tilesInCard.add(BonsaiTile(BonsaiTileType.WOOD))
                        } else {
                            tilesInCard.add(BonsaiTile(tile))
                        }
                    }

                    tilesInStorage.addAll(tilesInCard)

                    for (tile in tilesInStorage.shuffled()) {
                        val validPositions = possiblePositions.filter {
                            treeS.canPlaceTile(tile.type, it)
                        }

                        if (tilesInStorage.isNotEmpty() && validPositions.isNotEmpty()) {
                            val chosenPos = validPositions.random()
                            treeS.placeTile(tile, chosenPos)
                            tilesInStorage.remove(tile)
                            possiblePositions = treeS.getPossiblePlacements()
                        }
                    }

                    if (calculateReachedGoals().isNotEmpty()) {
                        game.currentState.state = States.CLAIMING_GOALS
                    } else {
                        game.currentState.state = States.TURN_END
                    }
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.DISCARDING -> {
                    val game = checkNotNull(root.game)
                    val toDiscard = mutableListOf<BonsaiTileType>()
                    val availableTypes = bot.storage.map { it.type }.toMutableList()

                    repeat(bot.storage.size - bot.maxCapacity) {
                        if (availableTypes.isNotEmpty()) {
                            val tileType = availableTypes.random()
                            toDiscard += tileType
                            availableTypes.remove(tileType)
                        }
                    }
                    println("Tries to discard $toDiscard")
                    playerActionS.discardTiles(toDiscard)
                    if (bot.storage.size <= bot.maxCapacity) {
                        game.currentState.state = States.TURN_END
                    }
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.CLAIMING_GOALS -> {
                    val reachableGoals = calculateReachedGoals()
                    if (reachableGoals.isNotEmpty()) {
                        playerActionS.claimGoal(reachableGoals.random())
                    }
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.REMOVE_TILES -> {
                    val occupied = checkNotNull(bot.tree.tiles.keys)
                    val leafs = occupied.filter { bot.tree.tiles[it] == BonsaiTileType.LEAF }.toTypedArray()
                    val tryThese = arrayOf(leafs.random())
                    treeS.removeTiles(tryThese)
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.MEDITATE -> return
                States.GAME_ENDED -> return
                States.TURN_END -> root.gameService.endTurn()
            }
        }
    }

    /**
     * Performs a single random "step" of the bot's logic based on the current [States].
     * Each call handles exactly one segment of the bot's turn:
     *
     * - If [States.CHOOSE_ACTION], randomly choose "meditate" or "cultivate."
     * - If [States.CULTIVATE], attempt to place tiles randomly on valid positions.
     * - If [States.CHOOSING_2ND_PLACE_TILES], pick between [BonsaiTileType.WOOD] or [BonsaiTileType.LEAF].
     * - If [States.USING_MASTER], pick a random tile type (excluding ANY).
     * - If [States.USING_HELPER], place tiles granted by the helper card in random valid positions.
     * - If [States.DISCARDING], discard tiles at random until under capacity.
     * - If [States.CLAIMING_GOALS], claim a randomly chosen reachable goal.
     * - Otherwise, end the turn.
     *
     * ## Preconditions:
     * - Must be called only if the current player is a bot.
     *
     * ## Postconditions:
     * - Executes exactly one segment of the turn in the current [States].
     * - May transition the game into the next [States].
     *
     * @throws IllegalStateException If the current player is not a bot or if there's no active game.
     */
    private fun makeRandomBotMove() {
        if (root.game == null || game.currentState.state == States.GAME_ENDED) {
            println("🚨 Game has ended. Bot cannot make a move.")
            return
        }

        val bot = currentState.currentPlayer

        require(bot.type == PlayerType.EASY_BOT || bot.type == PlayerType.HARD_BOT) {
            "Player must be a bot to make a bot move"
        }

        while (root.game != null && currentState.currentPlayer == bot) {
            when (currentState.state) {
                States.CHOOSE_ACTION -> {
                    var doMeditate = (0..1).random() == 0

                    if (bot.storage.isEmpty()) {
                        doMeditate = true
                    }

                    if (doMeditate) {
                        val remainingCards = game.currentState.centerCards.filterNotNull().size
                        playerActionS.meditate(((4 - remainingCards)..3).random())
                    } else {
                        playerActionS.cultivate()
                    }

                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.CULTIVATE -> {
                    var possiblePositions = treeS.getPossiblePlacements()
                    val tilesInStorage = bot.storage.toMutableList()

                    for (tile in tilesInStorage.shuffled()) {
                        val validPositions = possiblePositions.filter {
                            treeS.canPlaceTile(tile.type, it)
                        }

                        if (tilesInStorage.isNotEmpty() && validPositions.isNotEmpty()) {
                            val chosenPos = validPositions.random()
                            root.treeService.placeTile(tile, chosenPos)
                            tilesInStorage.remove(tile)
                            possiblePositions = root.treeService.getPossiblePlacements()
                        }
                    }

                    if (calculateReachedGoals().isNotEmpty()) {
                        game.currentState.state = States.CLAIMING_GOALS
                    } else {
                        game.currentState.state = States.TURN_END
                    }
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.CHOOSING_2ND_PLACE_TILES -> {
                    val choice = if ((0..1).random() == 0) BonsaiTileType.WOOD else BonsaiTileType.LEAF
                    playerActionS.chooseTile(choice)
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.USING_MASTER -> {
                    val chosenTile = BonsaiTileType.entries.filter { it != BonsaiTileType.ANY }.random()
                    game.currentState.currentPlayer.storage.add(BonsaiTile(chosenTile))
                    game.currentState.state = States.TURN_END
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.USING_HELPER -> {
                    var possiblePositions = treeS.getPossiblePlacements()
                    val game = checkNotNull(root.game)
                    val helperCard = checkNotNull(currentState.currentPlayer.discardPile.last())
                    val typesInCard = (helperCard as HelperCard).tiles.toMutableList()
                    val tilesInStorage = bot.storage.toMutableList()
                    val tilesInCard = mutableListOf<BonsaiTile>()

                    for (tile in typesInCard) {
                        if (tile == BonsaiTileType.ANY) {
                            tilesInCard.add(BonsaiTile(BonsaiTileType.WOOD))
                        } else {
                            tilesInCard.add(BonsaiTile(tile))
                        }
                    }

                    tilesInStorage.addAll(tilesInCard)

                    for (tile in tilesInStorage.shuffled()) {
                        val validPositions = possiblePositions.filter {
                            treeS.canPlaceTile(tile.type, it)
                        }

                        if (tilesInStorage.isNotEmpty() && validPositions.isNotEmpty()) {
                            val chosenPos = validPositions.random()
                            treeS.placeTile(tile, chosenPos)
                            tilesInStorage.remove(tile)
                            possiblePositions = treeS.getPossiblePlacements()
                        }
                    }

                    if (calculateReachedGoals().isNotEmpty()) {
                        game.currentState.state = States.CLAIMING_GOALS
                    } else {
                        game.currentState.state = States.TURN_END
                    }
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.DISCARDING -> {
                    val game = checkNotNull(root.game)
                    val toDiscard = mutableListOf<BonsaiTileType>()
                    val availableTypes = bot.storage.map { it.type }.toMutableList()

                    repeat(bot.storage.size - bot.maxCapacity) {
                        if (availableTypes.isNotEmpty()) {
                            val tileType = availableTypes.random()
                            toDiscard += tileType
                            availableTypes.remove(tileType)
                        }
                    }

                    println("Tries to discard $toDiscard")
                    playerActionS.discardTiles(toDiscard)
                    if (bot.storage.size <= bot.maxCapacity) {
                        game.currentState.state = States.TURN_END
                    }
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.CLAIMING_GOALS -> {
                    val reachableGoals = calculateReachedGoals()
                    if (reachableGoals.isNotEmpty()) {
                        playerActionS.claimGoal(reachableGoals.random())
                    }
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.REMOVE_TILES -> {
                    val occupied = checkNotNull(bot.tree.tiles.keys)
                    val leafs = occupied.filter { bot.tree.tiles[it] == BonsaiTileType.LEAF }.toTypedArray()
                    val tryThese = arrayOf(leafs.random())
                    treeS.removeTiles(tryThese)
                    Thread.sleep((game.botSpeed * 200).toLong())
                }

                States.MEDITATE -> return
                States.GAME_ENDED -> return
                States.TURN_END -> {
                    when (currentState.drawStack.size) {
                        10 -> currentPhase = BotPhase.MID
                        3 -> currentPhase = BotPhase.LATE
                    }
                    root.gameService.endTurn()
                }
            }
        }
    }

    /**
     * Calculates which goal tiles have been reached by the current player's bonsai.
     * - Includes threshold-based goals (wood, leaf, fruit, flower).
     * - Includes position-based goals (checked separately).
     * - Excludes renounced goals.
     *
     * @return A list of [GoalTile] objects that the current player is eligible to claim.
     */
    private fun calculateReachedGoals(): MutableList<GoalTile> {
        val currentGame = game.currentState
        val rootService = root

        //if goal is reached
        val reachedGoals = mutableListOf<GoalTile>().toMutableSet()
        for ((i,t) in currentGame.currentPlayer.tree.tiles) {
            when (t) {
                BonsaiTileType.WOOD, BonsaiTileType.FRUIT -> {
                    reachedGoals += currentGame.goalTiles.filter {
                        (it.type == GoalTileType.WOOD || it.type == GoalTileType.FRUIT) &&
                                rootService.treeService.getTileCount(t, currentGame.currentPlayer.tree) >= it.threshold
                    }
                }

                BonsaiTileType.LEAF -> {
                    val leafCount = rootService.treeService.countConnectedLeaves(i, currentGame.currentPlayer.tree)
                    reachedGoals += currentGame.goalTiles.filter { it.type == GoalTileType.LEAF && leafCount >= it.threshold }
                }

                BonsaiTileType.FLOWER -> {
                    val isOverhanging = when {
                        i.q < -1 - (i.r + 3) / 2 -> 1 //overhanging left
                        i.q > 3 - (i.r + 2) / 2 -> 2 //overhanging right
                        else -> 0
                    }
                    val amount = when (isOverhanging) {
                        1 -> currentGame.currentPlayer.tree.tiles.filter { it.key.q <= (-1 - ((it.key.r + 3) / 2)) && it.value == BonsaiTileType.FLOWER}
                        2 -> currentGame.currentPlayer.tree.tiles.filter { it.key.q >= (3 - ((it.key.r - 2) / 2)) && it.value == BonsaiTileType.FLOWER}
                        else -> emptyMap()
                    }
                    reachedGoals += currentGame.goalTiles.filter {
                        it.type == GoalTileType.FLOWER && amount.count() >= it.threshold
                    }
                }

                BonsaiTileType.ANY -> throw IllegalArgumentException("${BonsaiTileType.ANY} is not playable")
            }

            reachedGoals += currentGame.goalTiles.filter {
                it.type == GoalTileType.POSITION && it.threshold in 1..3 && rootService.treeService.checkBonsaiProtrusion(it.threshold)
            }
        }

        reachedGoals += currentGame.goalTiles.filter {
            it.type == GoalTileType.POSITION && it.threshold in 1..3 &&
                    rootService.treeService.checkBonsaiProtrusion(it.threshold)
        }

        val reachedGoalList = reachedGoals.toMutableList()
        //if goal is not already renounced
        for (i in reachedGoals.indices.reversed()) {
            val reachedGoal = reachedGoalList[i]
            if (currentGame.currentPlayer.renouncedGoals.contains(reachedGoal)) {
                reachedGoalList.removeAt(i)
            }
        }

        return reachedGoalList.toMutableList()
    }

    private fun pickBestMeditateCard(): Int {
        val bot = currentState.currentPlayer
        val availableCards = currentState.centerCards

        var bestIndex = -1
        var bestValue = Int.MIN_VALUE

        for (i in availableCards.indices) {
            val card = availableCards[i] ?: continue
            var score = 0

            when (card) {
                is GrowthCard -> {
                    score += when (currentPhase) {
                        BotPhase.EARLY -> 5
                        BotPhase.MID -> 3
                        BotPhase.LATE -> 0
                    }
                }

                is ParchmentCard -> {
                    score += when (currentPhase) {
                        BotPhase.EARLY -> 3
                        BotPhase.MID -> 5
                        BotPhase.LATE -> 6
                    }
                    val discardPile = currentState.currentPlayer.discardPile

                    score += when (card.type) {
                        ParchmentCardType.WOOD -> {
                            // Example: if the player has some condition for WOOD, else 0
                            // (replace with your own logic)
                            0
                        }

                        ParchmentCardType.HELPER -> {
                            // If the discardPile has >=3 HelperCards, +6 points, else +0
                            if (discardPile.filterIsInstance<HelperCard>().size >= 3) 6 else 0
                        }

                        ParchmentCardType.MASTER -> {
                            // If the discardPile has >=3 MasterCards, +6 points
                            if (discardPile.filterIsInstance<MasterCard>().size >= 3) 6 else 0
                        }

                        ParchmentCardType.GROWTH -> {
                            // If the discardPile has >=3 GrowthCards, +6 points
                            if (discardPile.filterIsInstance<GrowthCard>().size >= 3) 6 else 0
                        }

                        ParchmentCardType.LEAF -> {
                            if (treeS.getTileCount(BonsaiTileType.LEAF, bot.tree) > 7) 6 else 0
                        }

                        ParchmentCardType.FRUIT -> {
                            if (treeS.getTileCount(BonsaiTileType.FRUIT, bot.tree) > 2) 6 else 0

                        }

                        ParchmentCardType.FLOWER -> {
                            if (treeS.getTileCount(BonsaiTileType.FLOWER, bot.tree) > 3) 6 else 0
                        }
                    }
                }

                is HelperCard -> score += when (currentPhase) {
                    BotPhase.EARLY -> 3
                    BotPhase.MID -> 5
                    BotPhase.LATE -> 6
                }

                is MasterCard -> score += when (currentPhase) {
                    BotPhase.EARLY -> 4
                    BotPhase.MID -> 5
                    BotPhase.LATE -> 3
                }

                is ToolCard -> {
                    score += when (currentPhase) {
                        BotPhase.EARLY -> 8
                        BotPhase.MID -> 4
                        BotPhase.LATE -> 0
                    }
                }
            }

            when (i) {
                1 -> {
                    val preferredTile = if (currentPhase == BotPhase.EARLY || botMemory.deckSize > 10) {
                        BonsaiTileType.WOOD
                    } else {
                        BonsaiTileType.LEAF
                    }
                    score += getTileBonusValue(preferredTile)
                }

                2, 3 -> {
                    val (bonus1, bonus2) = botMemory.nextTileBonuses[i]
                    if (bonus1 != null) score += getTileBonusValue(bonus1)
                    if (bonus2 != null) score += getTileBonusValue(bonus2)
                }
            }

            if (score > bestValue) {
                bestValue = score
                bestIndex = i
            }
        }

        return if (bestIndex != -1) bestIndex else availableCards.indexOfFirst { it != null }
    }

    private fun getTileBonusValue(tileType: BonsaiTileType): Int {
        return when (tileType) {
            BonsaiTileType.WOOD -> 3
            BonsaiTileType.LEAF -> 3
            BonsaiTileType.FLOWER -> if (currentPhase == BotPhase.MID || currentPhase == BotPhase.LATE) 4 else 3
            BonsaiTileType.FRUIT -> if (currentPhase == BotPhase.MID || currentPhase == BotPhase.LATE) 4 else 3
            else -> 1
        }
    }
}
