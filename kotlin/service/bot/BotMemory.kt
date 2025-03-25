package service.bot

import entity.*

/**
 * Holds the bot's internal state and memory, including:
 * - How many Zen cards of each type remain (approx. counts)
 * - Observed opponent tile counts, goals, etc.
 */
data class BotMemory(
    var remainingCards: MutableList<ZenCard> = mutableListOf(
        GrowthCard(BonsaiTileType.WOOD, 0),
        GrowthCard(BonsaiTileType.WOOD, 1),
        GrowthCard(BonsaiTileType.LEAF, 2),
        GrowthCard(BonsaiTileType.LEAF, 3),
        GrowthCard(BonsaiTileType.FLOWER, 4),
        GrowthCard(BonsaiTileType.FLOWER, 5),
        GrowthCard(BonsaiTileType.FRUIT, 6),
        GrowthCard(BonsaiTileType.FRUIT, 7),

        // Helper Cards
        HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.WOOD), 14),
        HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.WOOD), 15),
        HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.WOOD), 16),
        HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.LEAF), 17),
        HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.LEAF), 18),
        HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.FLOWER), 19),
        HelperCard(arrayOf(BonsaiTileType.ANY, BonsaiTileType.FRUIT), 20),

        // Master Cards
        MasterCard(arrayOf(BonsaiTileType.WOOD, BonsaiTileType.WOOD), 21),
        MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.LEAF), 22),
        MasterCard(arrayOf(BonsaiTileType.WOOD, BonsaiTileType.LEAF), 23),
        MasterCard(arrayOf(BonsaiTileType.ANY), 24),
        MasterCard(arrayOf(BonsaiTileType.ANY), 25),
        MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.LEAF), 26),
        MasterCard(arrayOf(BonsaiTileType.LEAF, BonsaiTileType.FRUIT), 27),

        // Parchment Cards
        ParchmentCard(2, ParchmentCardType.MASTER, 34),
        ParchmentCard(2, ParchmentCardType.GROWTH, 35),
        ParchmentCard(2, ParchmentCardType.HELPER, 36),
        ParchmentCard(2, ParchmentCardType.FLOWER, 37),
        ParchmentCard(2, ParchmentCardType.FRUIT, 38),
        ParchmentCard(1, ParchmentCardType.LEAF, 39),
        ParchmentCard(1, ParchmentCardType.WOOD, 40),

        // Tool Cards
        ToolCard(2, 41),
        ToolCard(2, 42),
        ToolCard(2, 43)
    ),
    var deckSize: Int = 32,
    var nextTileBonuses: List<Pair<BonsaiTileType?, BonsaiTileType?>> = listOf(  // Slot refill bonuses
        null to null,  // Slot 0 (no bonus)
        BonsaiTileType.WOOD to BonsaiTileType.LEAF,  // Slot 1
        BonsaiTileType.WOOD to BonsaiTileType.FLOWER,  // Slot 2
        BonsaiTileType.LEAF to BonsaiTileType.FRUIT  // Slot 3
    )
)
