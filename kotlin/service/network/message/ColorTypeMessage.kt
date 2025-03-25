package service.network.message

import entity.*

/** All possible colors for a Bonsai Pot */
enum class  ColorTypeMessage {
    PURPLE,
    BLACK,
    BLUE,
    RED;

    /**
     * color types depend on color classes map
     */
    companion object {
        /**
         * Converts a [Colors] entity to its corresponding [ColorTypeMessage].
         *
         * @param color The [Colors] value to be converted.
         * @return The corresponding [ColorTypeMessage] representing the color.
         */
        fun toColorMsg(color: Colors): ColorTypeMessage {
            return when (color) {
                Colors.PURPLE -> PURPLE
                Colors.RED -> RED
                Colors.BLACK -> BLACK
                Colors.BLUE -> BLUE
            }
        }
    }

    /**
     * Converts a [ColorTypeMessage] back into its corresponding [Colors] entity.
     *
     * @return The corresponding [Colors] value.
     */
    fun toEntityColor(): Colors{
        return when(this) {
            PURPLE -> Colors.PURPLE
            BLACK -> Colors.BLACK
            BLUE -> Colors.BLUE
            RED -> Colors.RED
        }
    }
}
