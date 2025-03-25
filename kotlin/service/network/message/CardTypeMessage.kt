package service.network.message

import entity.*

/**
 * Enum representing different types of Zen cards in the game for network communication.
 */
enum class CardTypeMessage {
    TOOL,
    GROWTH,
    PARCHMENT,
    HELPER,
    MASTER;

    /**
     * trigger a [ZenCard] entity as corresponding [CardTypeMessage].
     */
    companion object{
        /**
         * Converts a [ZenCard] entity to its corresponding [CardTypeMessage].
         *
         * @param zenCard The [ZenCard] to be converted.
         * @return The corresponding [CardTypeMessage] representing the card type.
         */
        fun toMessage(zenCard: ZenCard): CardTypeMessage {
            return when (zenCard){
                is GrowthCard -> GROWTH
                is HelperCard -> HELPER
                is MasterCard -> MASTER
                is ParchmentCard -> PARCHMENT
                is ToolCard -> TOOL
            }
        }
    }
}