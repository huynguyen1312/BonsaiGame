package gui

/**
 * Enum that is used to provide the options for a ComboBox to choose whether a network game
 * should be started as host or joined as guest.
 */
enum class ConnectionRole {
    /**
     * Host was chosen for network game.
     */
    HOST,

    /**
     * Guest was chosen for network game.
     */
    GUEST
}