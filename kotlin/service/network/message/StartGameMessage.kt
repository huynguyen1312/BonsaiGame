package service.network.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Game action sent at the beginning of the game to initialize the game state.
 *
 * @property orderedPlayerNames The list of player names in turn order.
 * @property chosenGoalTiles The list of goal tiles chosen for this game.
 * @property orderedCards The ordered deck of cards, where each card is represented by its type
 * and the (0 based) index of the provided csv.
 * The top card is the tail of the list.
 * For example, a List(0..42) is the deck for 4 players, and entries 43, 44, 45, 46 are the revealed cards.
 */
@GameActionClass
data class StartGameMessage(
    val orderedPlayerNames: List<Pair<String, ColorTypeMessage>>,
    val chosenGoalTiles: List<GoalTileTypeMessage>,
    val orderedCards: List<Pair<CardTypeMessage, Int>>
) : GameAction() {

    /**
     * Provides a string representation of this [StartGameMessage] object, including details about
     * ordered player names with their associated colors, chosen goal tiles, and ordered cards.
     *
     * @return A string representation of the [StartGameMessage] object for debugging or logging purposes.
     */
    override fun toString(): String {
        var string = "StartGameMessage(\n"

        // add playerNames and their corresponding potColor to the string output
        string += "\torderedPlayerNames="
        for (player in orderedPlayerNames) {
            string += "\t${player.first} (${player.second.name})\n"
        }

        // add the chosenGoalTiles to the string output
        string += "\tchosenGoalTiles="
        for (goal in chosenGoalTiles) {
            string += "\t${goal.name}\n"
        }

        // add the orderedCards to the string output
        string += "\torderedCards="
        for (card in orderedCards) {
            string += "\t${card.first.name} (${card.second})\n"
        }

        return string
    }
}
