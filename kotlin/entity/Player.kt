package entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Represents a player in the Bonsai game.
 *
 * @property name The player's name
 * @property maxCapacity Maximum capacity for storage
 * @property growthLimit Limits for growth in different categories, in order WOOD, LEAF, FLOWER, FRUIT, and ANY
 * @property type The type of player
 * @property remainingGrowth Remaining growth values in different categories
 * @property discardPile List of discarded [ZenCard]s
 * @property storage List of stored [BonsaiTile]s
 * @property goalTiles Achieved [GoalTile]s
 * @property tree The player's [Tree]
 * @property growthCardPile List of [GrowthCard]s
 * @property toolCardPile List of [ToolCard]s
 * @property renouncedGoals List of renounced [GoalTile]s
 *
 * @throws IllegalArgumentException if [growthLimit] does not have exactly 5 elements
 */
@Serializable
data class Player(
    val name: String,
    val type: PlayerType,
    val color: Colors,
    val storage: MutableList<BonsaiTile> = mutableListOf(),
) {
    var remainingGrowth: IntArray = intArrayOf(0, 0, 0, 0, 0)
    val growthLimit: IntArray = intArrayOf(1, 1, 0, 0, 1)
    val discardPile: MutableList<ZenCard> = mutableListOf()

    @Serializable(with = ArrayDequeSerializer::class)
    val goalTiles: ArrayDeque<GoalTile> = ArrayDeque()
    val tree: Tree = Tree()
    val growthCardPile: MutableList<GrowthCard> = mutableListOf()
    val toolCardPile: MutableList<ToolCard> = mutableListOf()
    var maxCapacity: Int = 5
    val renouncedGoals: MutableList<GoalTile> = mutableListOf()


    /**
     * Creates a deep copy of the current player.
     */
    fun copy(): Player {
        val storageCopy: MutableList<BonsaiTile> = mutableListOf()
        storage.forEach { storageCopy.add(it) }
        val copiedPlayer = Player(name, type, color, storageCopy)
        remainingGrowth.copyInto(copiedPlayer.remainingGrowth)
        growthLimit.copyInto(copiedPlayer.growthLimit)
        discardPile.forEach { copiedPlayer.discardPile.add(it.copy()) }
        goalTiles.forEach { copiedPlayer.goalTiles.add(it.copy()) }
        copiedPlayer.tree.tiles.forEach { copiedPlayer.tree.tiles[it.key] = it.value }
        growthCardPile.forEach { copiedPlayer.growthCardPile.add(it.copy()) }
        toolCardPile.forEach { copiedPlayer.toolCardPile.add(it.copy()) }
        copiedPlayer.maxCapacity = maxCapacity
        renouncedGoals.forEach { copiedPlayer.renouncedGoals.add(it.copy()) }

        return copiedPlayer
    }

    /**
     * toString implementation for BonsaiTile
     */
    override fun toString(): String {
        return "Player(name='$name'," +
                "maxCapacity=$maxCapacity, " +
                "growthLimit=${growthLimit.contentToString()}, " +
                "type=$type, currentGrowth=${remainingGrowth.contentToString()}, " +
                "discardPile=$discardPile, " + "storage=$storage, " +
                "goalTiles=$goalTiles, " + "tree=$tree, " +
                "growthCardPile=$growthCardPile, " + "toolCardPile=$toolCardPile)" +
                "renouncedGoals=$renouncedGoals"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (name != other.name) return false
        if (type != other.type) return false
        if (storage != other.storage) return false
        if (!remainingGrowth.contentEquals(other.remainingGrowth)) return false
        if (!growthLimit.contentEquals(other.growthLimit)) return false
        if (discardPile != other.discardPile) return false
        if (goalTiles != other.goalTiles) return false
        if (tree != other.tree) return false
        if (growthCardPile != other.growthCardPile) return false
        if (toolCardPile != other.toolCardPile) return false
        if (maxCapacity != other.maxCapacity) return false
        return renouncedGoals == other.renouncedGoals
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + storage.hashCode()
        result = 31 * result + remainingGrowth.contentHashCode()
        result = 31 * result + growthLimit.contentHashCode()
        result = 31 * result + discardPile.hashCode()
        result = 31 * result + goalTiles.hashCode()
        result = 31 * result + tree.hashCode()
        result = 31 * result + growthCardPile.hashCode()
        result = 31 * result + toolCardPile.hashCode()
        result = 31 * result + maxCapacity
        result = 31 * result + renouncedGoals.hashCode()
        return result
    }

    /**
     * A helper class to serialize ArrayDeque since it is only used in Player and does not have a built-in serializer.
     */
    class ArrayDequeSerializer<T>(private val elementSerializer: KSerializer<T>) : KSerializer<ArrayDeque<T>> {
        private val listSerializer = ListSerializer(elementSerializer)
        override val descriptor = listSerializer.descriptor

        override fun serialize(encoder: Encoder, value: ArrayDeque<T>) {
            listSerializer.serialize(encoder, value.toList())
        }

        override fun deserialize(decoder: Decoder): ArrayDeque<T> {
            return ArrayDeque(listSerializer.deserialize(decoder))
        }
    }
}

