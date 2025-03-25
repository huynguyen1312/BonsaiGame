package service.network

import entity.*
import service.*
import service.network.message.*

/**
 * Handles the network communication for the game.
 * This class manages connections, sending messages, and handling received data.
 *
 * @param root The [RootService] instance used for managing the game state.
 */
class NetworkService(val root: RootService) : AbstractRefreshingService() {
    /**
     * address from server and game ID
     */
    companion object {
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"
        const val GAME_ID = "Bonsai"
    }

    var client: BonsaiNetworkClient? = null
        private set
    var connection: ConnectionState = ConnectionState.DISCONNECTED
        private set
    var players: MutableList<String> = mutableListOf()
    val playerTypes = mutableListOf<PlayerType>()

    /**
     * Hosts a new online game session.
     *
     * @param name The name of the player hosting the game.
     * @param playerType The type of the player (Local, Easy Bot, Hard Bot).
     * @throws IllegalStateException If the connection fails.
     */
    fun hostGame(name: String, playerType: PlayerType){
        if(!connect(name)){
            error("Connection failed!")
        }
        updateConnectionState(ConnectionState.CONNECTED)

        players.add(name)
        client?.createGame(GAME_ID, "Welcome!")
        playerTypes.add(playerType)
        updateConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
        root.waitForState(ConnectionState.WAITING_FOR_GUEST)
    }


    /**
     * Starts a hosted game session with predefined settings.
     *
     * @param colors is the local players color
     * @param goalTileType is the chosen goal tiles. Must be three.
     * @param randomizedGoal If true, goal tiles will be randomly selected.
     */
    fun createHostedGame(
        colors: MutableList<Colors>,
        goalTileType: MutableList<GoalTileType>,
        randomizedGoal: Boolean = false){

        println("start createHostedGame")
        val gameConfig = GameConfig(
            0.0,
            players,
            playerTypes,
            hotSeat = false,
            randomizedPlayerOrder = false,
            randomizedGoal = randomizedGoal,
            goalTiles = goalTileType,
            color = colors
        )
        println("GameConfig created: $gameConfig")

        println("before sendGameMessage")
        sendStartGameMessage(gameConfig)
        onAllRefreshables { refreshAfterGameStart() }
    }

    /**
     * Disconnects from the current game session.
     * If the player is in a session, they will leave before disconnecting.
     */
    fun disconnect(){
        client?.apply {
            if(sessionID != null) leaveGame("Goodbye!")
            if(isOpen) disconnect()
        }
        client = null
        updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * Joins an existing online game session.
     *
     * @param sessionID The session ID of the game to join.
     * @param name The player's name.
     * @throws IllegalStateException If the connection fails.
     */
    fun joinGame(sessionID: String, name: String){
        if(!connect(name)){
            error("Connection failed!")
        }
        updateConnectionState(ConnectionState.CONNECTED)

        client?.joinGame(sessionID, "Hello")

        updateConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
        playerTypes.add(PlayerType.LOCAL)
        root.waitForState(ConnectionState.WAITING_FOR_INIT)
    }

    /**
     * Establishes a connection to the game server.
     *
     * @param name The name of the player.
     * @return `true` if the connection was successful, `false` otherwise.
     * @throws IllegalStateException If the player is already connected.
     */
    fun connect(name: String): Boolean {
        require(connection == ConnectionState.DISCONNECTED && client == null){
            "Already connected to another game."
        }
        require(name.isNotBlank()){"Player must have a name."}

        val newClient =
            BonsaiNetworkClient(
                playerName = name,
                host = SERVER_ADDRESS,
                networkService = this
            )

        return if(newClient.connect()){
            this.client = newClient
            true
        } else {
            false
        }

    }

    /**
     * Sends a message to all players to start the game with the given configuration.
     *
     * @param gameConfig The configuration for the game session.
     * @throws IllegalStateException If the game is not ready to start.
     */
    private fun sendStartGameMessage(gameConfig: GameConfig){
        println("sendStartGameMessage")
        check(connection == ConnectionState.WAITING_FOR_GUEST){
            "Not ready to start a game now."
        }
        root.gameService.startGame(gameConfig)
        val game = requireNotNull(root.game){"Something went wrong while creating the game."}

        val playersMsg: List<Pair<String, ColorTypeMessage>> =
            game.currentState.players.map { it.name to ColorTypeMessage.toColorMsg(it.color) }

        val goals: List<GoalTileTypeMessage> = gameConfig.goalTiles.map {
            GoalTileTypeMessage.toGoalTileMsg(it)
        }

        var cards: List<Pair<CardTypeMessage, Int>> = game.currentState.drawStack.map {
            CardTypeMessage.toMessage(it) to it.id
        }
        cards = cards.plus(game.currentState.centerCards.map {
            val card = requireNotNull(it)
            CardTypeMessage.toMessage(it) to it.id
        })

        val message = StartGameMessage(
            playersMsg,
            goals,
            cards
        )
        println(message)

        client?.sendGameActionMessage(message)
        if(game.currentState.currentPlayer.type == PlayerType.LOCAL){
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }
        else{
            updateConnectionState((ConnectionState.WAITING_FOR_OPPONENT))
        }
    }

    /**
     * After the Host has started the game, the start game message will notify all other
     * players about the game config.
     *
     * @param message contains the [StartGameMessage]
     *
     * 1) Read out the player order, given in pair with (name,color).
     * 2) Read out color in same order as players.
     * 3) playerTypes for now everything is set on [PlayerType.ONLINE]
     * 4) Read out chosen goalTileTypes.
     * 5) Save all information in [GameConfig]
     * 6) Read out Card order.
     */
    fun startNewJoinedGame(message: StartGameMessage){
        val players = mutableListOf<String>()
        val colors = mutableListOf<Colors>()
        val playerTypes = mutableListOf<PlayerType>()
        message.orderedPlayerNames.forEach {
            players.add(it.first)
            colors.add(it.second.toEntityColor())
            if(client?.playerName == it.first) {
                playerTypes.add(PlayerType.LOCAL)
            } else {
                playerTypes.add(PlayerType.ONLINE)
            }
        }

        val goals = mutableListOf<GoalTileType>()
        message.chosenGoalTiles.forEach { goals.add(it.toGoalTileType()) }

        val gameConfig = GameConfig(
            0.0, players, playerTypes, hotSeat = false, randomizedPlayerOrder = false,
            randomizedGoal = false, goalTiles = goals, color = colors
        )

        val cards = mutableListOf<ZenCard>()
        message.orderedCards.forEach {
           cardMap[it.second]?.let { it1 -> cards.add(it1) }
            //game currently is not running just yet, so how could i access the map
        }
        cards.toTypedArray()
        root.gameService.startGame(gameConfig, cards)
        onAllRefreshables { refreshAfterGameStart() }
    }

    /**
     * Sends a cultivate move message to the server.
     *
     * @param message The message containing move details.
     */
    fun sendCultivateMessage(message: NetworkMessageHelper){
        val remove = mutableListOf<Pair<Int, Int>>()
        remove.addAll(message.removedTiles.map { Pair(it.q, it.r) })

        val played = mutableListOf<Pair<TileTypeMessage, Pair<Int,Int>>>()
        played.addAll(message.placedTiles.map {
            Pair(
                TileTypeMessage.toTileTypeMsg(it.first),
                Pair(it.second.q, it.second.r)
            )  })

        val claimed = mutableListOf<Pair<GoalTileTypeMessage, Int>>()
        val renounced = mutableListOf<Pair<GoalTileTypeMessage, Int>>()
        for(goal in message.goals){
            val tile = GoalTileTypeMessage.toGoalTileMsg(goal.first.type)
            if(goal.second){
                claimed.add(Pair(tile, findGoalTier(goal.first)))
            }
            else{
                renounced.add(Pair(tile, findGoalTier(goal.first)))
            }
        }
        val cultivateMessage = CultivateMessage(remove, played, claimed, renounced)
        client?.sendGameActionMessage(cultivateMessage)
        updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
    }


    /**
     * Sends the meditate Message.
     *
     * @param message is an object of type NetworkMeditateHelper which saves information throughout
     * the turn.
     */
    fun sendMeditateMessage(message: NetworkMessageHelper){
        val remove = mutableListOf<Pair<Int, Int>>()
        remove.addAll(message.removedTiles.map { Pair(it.q, it.r) })

        val played = mutableListOf<Pair<TileTypeMessage, Pair<Int,Int>>>()
        played.addAll(message.placedTiles.map {
            Pair(
                TileTypeMessage.toTileTypeMsg(it.first),
                Pair(it.second.q, it.second.r)
            )  })

        val drawnTiles = mutableListOf<TileTypeMessage>()
        message.drawnTiles.forEach{
            drawnTiles.add(TileTypeMessage.toTileTypeMsg(it))
        }

        val claimed = mutableListOf<Pair<GoalTileTypeMessage, Int>>()
        val renounced = mutableListOf<Pair<GoalTileTypeMessage, Int>>()
        for(goal in message.goals){
            val tile = GoalTileTypeMessage.toGoalTileMsg(goal.first.type)
            if(goal.second){
                claimed.add(Pair(tile, findGoalTier(goal.first)))
            }
            else{
                renounced.add(Pair(tile, findGoalTier(goal.first)))
            }
        }

        val discard = mutableListOf<TileTypeMessage>()
        message.discardedTiles.forEach{
            TileTypeMessage.toTileTypeMsg(it)
        }

        val meditateMessage = MeditateMessage(remove, message.cardIndex, played,
            drawnTiles, claimed, renounced, discard)
        client?.sendGameActionMessage(meditateMessage)
        updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
    }


    /**
     * Helper Function to find the tier of a claimed/renounced goal. We don't save this information
     * as we deemed it unimportant however the gameActionMessage relies on it.
     */
    private fun findGoalTier(goalTile: GoalTile): Int{
        var tier = when (goalTile.points){
            in 5..8 -> 1
            in 10..11 -> 2
            in 13..16 -> 3
            else -> 0
        }
        if(tier == 0){
            tier = if(goalTile.points == 9){
                if(goalTile.type==GoalTileType.LEAF){
                    2
                } else{
                    1
                }
            } else{
                if(goalTile.type==GoalTileType.LEAF){
                    3
                } else 2
            }
        }
        return tier
    }

    /**
     * Sets ConnectionState to newState.
     */
    fun updateConnectionState(newState: ConnectionState){
        this.connection = newState
    }

    /**
     * Maps the cards to their ids.
     */
    private val cardMap = mapOf(
        0 to GrowthCard(BonsaiTileType.WOOD, id = 0),
        1 to GrowthCard(BonsaiTileType.WOOD, id = 1),
        2 to GrowthCard(BonsaiTileType.LEAF, id = 2),
        3 to GrowthCard(BonsaiTileType.LEAF, id = 3),
        4 to GrowthCard(BonsaiTileType.FLOWER, id = 4),
        5 to GrowthCard(BonsaiTileType.FLOWER, id = 5),
        6 to GrowthCard(BonsaiTileType.FRUIT, id = 6),
        7 to GrowthCard(BonsaiTileType.FRUIT, id = 7),
        8 to GrowthCard(BonsaiTileType.WOOD, id = 8),                                                       // 3
        9 to GrowthCard(BonsaiTileType.LEAF, id = 9),                                                       // 3
        10 to GrowthCard(BonsaiTileType.LEAF, id = 10),                                                     // 3
        11 to GrowthCard(BonsaiTileType.FLOWER, id = 11),                                                   // 3
        12 to GrowthCard(BonsaiTileType.WOOD, id = 12),                                                     // 4
        13 to GrowthCard(BonsaiTileType.FRUIT, id = 13),                                                    // 4
        14 to HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.WOOD), id = 14),
        15 to HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.WOOD), id = 15),
        16 to HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.WOOD), id = 16),
        17 to HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.LEAF), id = 17),
        18 to HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.LEAF), id = 18),
        19 to HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.FLOWER), id = 19),
        20 to HelperCard(arrayOf(BonsaiTileType.ANY,BonsaiTileType.FRUIT), id = 20),
        21 to MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.WOOD), id = 21),
        22 to MasterCard(arrayOf(BonsaiTileType.LEAF,BonsaiTileType.LEAF), id = 22),
        23 to MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.LEAF), id = 23),
        24 to MasterCard(arrayOf(BonsaiTileType.ANY), id = 24),
        25 to MasterCard(arrayOf(BonsaiTileType.ANY), id = 25),
        26 to MasterCard(arrayOf(BonsaiTileType.LEAF,BonsaiTileType.LEAF), id = 26),
        27 to MasterCard(arrayOf(BonsaiTileType.LEAF,BonsaiTileType.FRUIT   ), id = 27),
        28 to MasterCard(arrayOf(BonsaiTileType.ANY), id = 28),                                             // 3
        29 to MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.LEAF), id = 29),                        // 3
        30 to MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.LEAF), id = 30),                        // 3
        31 to MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.LEAF,BonsaiTileType.FLOWER), id = 31),  // 3
        32 to MasterCard(arrayOf(BonsaiTileType.WOOD,BonsaiTileType.LEAF,BonsaiTileType.FRUIT), id = 32),   // 3
        33 to MasterCard(arrayOf(BonsaiTileType.LEAF,BonsaiTileType.FLOWER,BonsaiTileType.FLOWER), id = 33),// 4
        34 to ParchmentCard(2,ParchmentCardType.MASTER,id = 34),
        35 to ParchmentCard(2,ParchmentCardType.GROWTH,id = 35),
        36 to ParchmentCard(2,ParchmentCardType.HELPER,id = 36),
        37 to ParchmentCard(2,ParchmentCardType.FLOWER, id = 37),
        38 to ParchmentCard(2,ParchmentCardType.FRUIT, id = 38),
        39 to ParchmentCard(1, ParchmentCardType.LEAF, id = 39),
        40 to ParchmentCard(1,ParchmentCardType.WOOD, id = 40),
        41 to ToolCard(id = 41),
        42 to ToolCard(id = 42),
        43 to ToolCard(id = 43),
        44 to ToolCard(id = 44),                                                                            // 3
        45 to ToolCard(id = 45),                                                                            // 3
        46 to ToolCard(id = 46)                                                                             // 4
    )

    private fun RootService.waitForState(state: ConnectionState, timeout: Int = 5000) {
        var timePassed = 0
        while (timePassed < timeout) {
            if (networkService.connection == state)
                return
            else {
                Thread.sleep(100)
                timePassed += 100
            }
        }
        error("Did not arrive at state $state after waiting $timeout ms")
    }
}
