package gui
/**
 * Enum that is used to provide the options for a ComboBox to choose whether a network game
 * should be started or joined as an easy bot, a hard bot or a real human.
 */
enum class BotMode {
    /**
     * player as an easy bot
     */
    EASY_BOT,
    /**
     * player as a hard bot
     */
    HARD_BOT,
    /**
     * player as a real human
     */
    HUMAN
}