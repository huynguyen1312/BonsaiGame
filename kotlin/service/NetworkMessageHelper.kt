package service

import entity.*

/**
 * Game action sen when a player performs meditate.
 *
 * @property meditate true if the action is meditate, false if cultivate
 * @property removedTiles contains the positions of removed tiles.
 * @property cardIndex the chosen cards position in the center [0,3].
 * A value of `-1` indicates no card was selected.
 * @property placedTiles contains the TileType and Position of all placed tiles.
 * @property drawnTiles contains all tiles that have been drawn. Starting with the chosen
 * card if card at index 1 was chosen, and all other tiles, that were drawn due to the card.
 * @property goals A list of goal tiles that were achieved during the turn,
 * along with a Boolean indicating whether they were claimed (`true`) or renounced (`false`).
 * @property discardedTiles A list of tiles that were discarded due to exceeding
 * the maximum storage capacity.
 */
class NetworkMessageHelper {
    var meditate: Boolean = true
    var removedTiles = mutableListOf<Vector>()
    var cardIndex: Int = -1
    var placedTiles = mutableListOf<Pair<BonsaiTileType,Vector>>()
    var drawnTiles = mutableListOf<BonsaiTileType>()
    var goals = mutableListOf<Pair<GoalTile, Boolean>>()
    var discardedTiles = mutableListOf<BonsaiTileType>()

    /**
     * Clears all stored game action data in this helper class.
     *
     * This method resets all stored values, ensuring that the object is in its initial state
     * before being reused for another action.
     *
     * Postconditions:
     * - `removedTiles` list is emptied.
     * - `cardIndex` is reset to `-1`.
     * - `placedTiles`, `drawnTiles`, `goals`, and `discardedTiles` lists are cleared.
     */
    fun clear(){
        removedTiles.clear()
        cardIndex = -1
        placedTiles.clear()
        drawnTiles.clear()
        goals.clear()
        discardedTiles.clear()
    }

    /**
     * Adds predefined drawn tiles based on the chosen card index.
     *
     * This function determines which tiles are drawn when selecting a card from the center.
     * It updates the `drawnTiles` list based on the provided card index.
     *
     * @param cardIndex The index of the chosen card in the center area [0-3].
     *
     * Preconditions:
     * - The `cardIndex` must be either `2` or `3`, as these correspond to predefined tile draws.
     *
     * Postconditions:
     * - If `cardIndex == 2`, a `WOOD` and `FLOWER` tile are added to `drawnTiles`.
     * - If `cardIndex == 3`, a `FRUIT` and `LEAF` tile are added to `drawnTiles`.
     * - If the index does not match `2` or `3`, no tiles are added.
     */
    fun getDrawnTiles(cardIndex: Int){
        when (cardIndex){
            2 -> {
                drawnTiles.add(BonsaiTileType.WOOD)
                drawnTiles.add(BonsaiTileType.FLOWER)}
            3 -> {
                drawnTiles.add(BonsaiTileType.FRUIT)
                drawnTiles.add(BonsaiTileType.LEAF)
            }

        }
    }
}