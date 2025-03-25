package entity

import kotlinx.serialization.Serializable

/**
 * Represents the current state of a Bonsai game.
 *
 * @property players Array of players participating in the game (2-4 players)
 * @property currentPlayerIndex Index of the current player in the [players] array
 * @property centerCards Array of four [ZenCard]s available in the center.
 * @property state Current phase of the game as defined in [States]
 * @property goalTiles List of nine available [GoalTile]s
 * @property lastRound Indicates if the game has entered its final round (can only be set to true)
 * @property finalPlayer Index of the player who triggered the game end (-1 if not set)
 * @property currentPlayer The currently active player (derived from [currentPlayerIndex])
 * @property drawStack The stack of cards that can be drawn from
 *
 * @throws IllegalArgumentException If:
 * - The number of center cards is not exactly 4
 * - The number of players is not between 2 and 4
 * - The number of goal tiles is not exactly 9
 */
@Serializable
data class GameState(
    val players: Array<Player>,
    var currentPlayerIndex: Int,
    var centerCards: Array<ZenCard?>,
    var state: States, //TODO add default state
    var goalTiles: MutableList<GoalTile>,
    var drawStack: MutableList<ZenCard>,
) {
    var lastRound: Boolean = false
    var finalPlayer: Int = -1
        set(value) {
            require(value != -1) { "Can't set final Player to the antoher player if it is already set to $field" }
            require(value + 1 in 1..players.size) { "Can't set final Player to a value that does not exist" }
            field = value
        }

    val currentPlayer: Player
        get() = players[currentPlayerIndex]

    var currentlyPlayedCard: ZenCard? = null

    init {
        require(players.size in 2..4) { "Current players must have 4 players, currently ${players.size}" }
    }


    /**
     * Creates a deep copy of the current game state.
     */
    fun copy(): GameState {
        var playersCopy: Array<Player> = arrayOf()
        players.forEach { playersCopy += it.copy() }
        var centerCardsCopy: Array<ZenCard?> = arrayOf()
        centerCards.forEach { centerCardsCopy += it?.copy() }
        val goalTilesCopy: MutableList<GoalTile> = mutableListOf()
        goalTiles.forEach { goalTilesCopy.add(it) }
        val drawStackCopy: MutableList<ZenCard> = mutableListOf()
        drawStack.forEach { drawStackCopy.add(it) }

        val copiedGameState = GameState(
            players = playersCopy,
            currentPlayerIndex = currentPlayerIndex,
            centerCards = centerCardsCopy,
            state = state,
            goalTiles = goalTilesCopy,
            drawStack = drawStackCopy
        )
        copiedGameState.lastRound = lastRound
        if(this.finalPlayer != -1) copiedGameState.finalPlayer = finalPlayer
        copiedGameState.currentlyPlayedCard = currentlyPlayedCard?.copy()
        return copiedGameState
    }

    /**
     * toString implementation for BonsaiTile
     */
    override fun toString(): String {
        return "GameState(currentPlayers=${players.contentToString()}, " +
                "currentPlayerIndex=$currentPlayerIndex, " +
                "centerCards=${centerCards.contentToString()}, " +
                "state=$state, " + "goalTiles=$goalTiles, " +
                "lastRound=$lastRound, " +
                "finalPlayer=$finalPlayer, " +
                "currentPlayer=$currentPlayer)" +
                "drawStack=$drawStack"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!players.contentEquals(other.players)) return false
        if (currentPlayerIndex != other.currentPlayerIndex) return false
        if (!centerCards.contentEquals(other.centerCards)) return false
        if (state != other.state) return false
        if (goalTiles != other.goalTiles) return false
        if (drawStack != other.drawStack) return false
        if (lastRound != other.lastRound) return false
        return finalPlayer == other.finalPlayer
    }

    override fun hashCode(): Int {
        var result = players.contentHashCode()
        result = 31 * result + currentPlayerIndex
        result = 31 * result + centerCards.contentHashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + goalTiles.hashCode()
        result = 31 * result + drawStack.hashCode()
        result = 31 * result + lastRound.hashCode()
        result = 31 * result + finalPlayer
        return result
    }

}
