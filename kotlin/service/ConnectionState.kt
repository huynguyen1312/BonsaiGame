package service

/**
 * A collection of possible states for the connection to the game server.
 */
enum class ConnectionState {
    /**
     * No connection is active.
     * Initial State at the start of the program.
     * State after active connection is closed.
     */
    DISCONNECTED,

    /**
     * Connected to server, but no game started or joined yet.
     */
    CONNECTED,

    /**
     * hostGame request sent to sever. Waiting for confirmation.
     */
    WAITING_FOR_HOST_CONFIRMATION,

    /**
     * hostGame accepted. Host is waiting for other players.
     */
    WAITING_FOR_GUEST,

    /**
     * joinGame request sent to server. Waiting for confirmation.
     */
    WAITING_FOR_JOIN_CONFIRMATION,

    /**
     * Joined game as guest (non-host). Waiting for gameStart/host sending init message.
     */
    WAITING_FOR_INIT,

    /**
     * Game is running. It is this players turn. They can play their turn according to rules.
     */
    PLAYING_MY_TURN,

    /**
     * Game is running. It is another players turn. Waiting for opponents turnMessage.
     */
    WAITING_FOR_OPPONENT,
}