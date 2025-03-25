package service

import entity.*
import gui.Refreshable

/**
 * Helper class for meditating
 *
 * @property gameState the current game state
 */
class MeditateHelper(
    var gameState: GameState
) {
    private val currentPlayer: Player
        get() = gameState.currentPlayer

    /**
     * Refills the card slots in the center of the board by shifting existing cards to the right
     * and drawing a new card from the draw stack if available.
     *
     * **Postconditions:**
     * - All existing cards in the center are moved as far right as possible.
     * - A new card is drawn from the draw stack and placed in the leftmost slot (index 0), if the stack is not empty.
     */
    fun refillCards() {
        // Move all cards to the as far right as they can
        gameState.centerCards.moveElementsToRight()

        // Draw new card
        if (gameState.drawStack.isNotEmpty()) {
            gameState.centerCards[0] = gameState.drawStack.removeLast()
        }
    }

    /**
     * Resolves the effect of the currently played card.
     *
     * **Preconditions:**
     * - A card must be actively played (`gameState.currentlyPlayedCard` must not be null).
     *
     * **Postconditions:**
     * - The effect of the card is applied to the game state.
     * - The state of the game is updated accordingly.
     * - A refresh function is returned, which updates the UI after the meditation action.
     *
     * @param cardIndex The index of the selected card in the center [0-3].
     * @return A lambda function that triggers the appropriate UI refresh.
     * @throws IllegalArgumentException If no card is currently being played.
     */
    fun resolveCardEffect(cardIndex: Int): Refreshable.() -> Unit {
        // For each card resolve its effect
        when (val card = requireNotNull(gameState.currentlyPlayedCard) { "No card is currently played" }) {
            is ParchmentCard -> handleParchmentCard(card)
            is ToolCard -> handleToolCard(card)
            is GrowthCard -> handleGrowthCard(card)
            is MasterCard -> handleMasterCard(card)
            is HelperCard -> handleHelperCard(card)
        }
        return { refreshAfterMeditate(cardIndex) }
    }

    /**
     * Handles the effect of a ParchmentCard
     *
     * @param card the ParchmentCard to handle
     */
    private fun handleParchmentCard(card: ParchmentCard) {
        currentPlayer.discardPile.add(currentPlayer.discardPile.size, card)
        // Set currentPlayedCard to null
        gameState.currentlyPlayedCard = null
        gameState.state = States.TURN_END
    }

    /**
     * Handles the effect of a ToolCard
     *
     * @param card the ToolCard to handle
     */
    private fun handleToolCard(card: ToolCard) {
        currentPlayer.toolCardPile.add(card)
        currentPlayer.maxCapacity += 2
        gameState.state = States.TURN_END
    }

    /**
     * Handles the effect of a GrowthCard
     *
     * @param card the GrowthCard to handle
     */
    private fun handleGrowthCard(card: GrowthCard) {
        currentPlayer.growthCardPile.add(card)
        when (card.tile) {
            BonsaiTileType.WOOD -> currentPlayer.growthLimit[0]++
            BonsaiTileType.LEAF -> currentPlayer.growthLimit[1]++
            BonsaiTileType.FLOWER -> currentPlayer.growthLimit[2]++
            BonsaiTileType.FRUIT -> currentPlayer.growthLimit[3]++
            else -> throw IllegalArgumentException("Growth card must have a specific tile")
        }
        // Set currentPlayedCard to null
        gameState.currentlyPlayedCard = null
        gameState.state = States.TURN_END
    }

    /**
     * Handles the effect of a MasterCard
     *
     * @param card the MasterCard to handle
     */
    private fun handleMasterCard(card: MasterCard) {
        currentPlayer.discardPile.add(card)
        for (tile in card.tiles) {
            // Only add the tile if it is not ANY
            if (tile != BonsaiTileType.ANY) {
                currentPlayer.storage.add(BonsaiTile(tile))
            }
        }
        if (card.tiles.contains(BonsaiTileType.ANY)) {
            gameState.state = States.USING_MASTER
        } else {
            gameState.state = States.TURN_END
            // Set currentPlayedCard to null
            gameState.currentlyPlayedCard = null
        }
    }

    /**
     * Handles the effect of a HelperCard
     *
     * @param card the HelperCard to handle
     */
    private fun handleHelperCard(card: HelperCard) {
        currentPlayer.discardPile.add(currentPlayer.discardPile.size, card)
        gameState.state = States.USING_HELPER
        for (tile in card.tiles) {
            when (tile) {
                BonsaiTileType.WOOD -> currentPlayer.remainingGrowth[0]++
                BonsaiTileType.LEAF -> currentPlayer.remainingGrowth[1]++
                BonsaiTileType.FLOWER -> currentPlayer.remainingGrowth[2]++
                BonsaiTileType.FRUIT -> currentPlayer.remainingGrowth[3]++
                BonsaiTileType.ANY -> currentPlayer.remainingGrowth[4]++
            }
        }
        // Set currentPlayedCard to null
        gameState.currentlyPlayedCard = null
    }

    /**
     * Marks the game as being in its last round.
     *
     * **Preconditions:**
     * - The game must be in progress.
     * - The last round must not have already been initiated.
     *
     * **Postconditions:**
     * - If the last round has not yet started, it is now set to `true`.
     * - The current player is marked as the final player (`gameState.finalPlayer`).
     */
    fun initializeLastRound() {
        // Set lastRound to true
        if (!gameState.lastRound) {
            gameState.lastRound = true
            gameState.finalPlayer = gameState.currentPlayerIndex
        }
    }

    /**
     * Moves all elements of the array to the right
     */
    fun <T> Array<T?>.moveElementsToRight() {
        var insertPos = this.size - 1
        // Go through the array from right to left
        for (i in this.indices.reversed()) {
            // If the element is not null, move it to the right
            if (this[i] != null) {
                // If the insert position is not the same as the current position, set the current position to null
                this[insertPos] = this[i]
                if (insertPos != i) {
                    this[i] = null
                }
                insertPos--
            }
        }
    }
}