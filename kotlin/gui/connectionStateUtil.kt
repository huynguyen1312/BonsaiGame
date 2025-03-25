package gui

import service.ConnectionState

/**
 * provides a corresponding text for this [ConnectionState] to be displayed in the GUI.
 */
fun ConnectionState.toUIText() =
    when(this) {
        ConnectionState.DISCONNECTED -> "Disconnected."
        ConnectionState.CONNECTED -> "Connected."
        ConnectionState.WAITING_FOR_HOST_CONFIRMATION -> "Waiting for server to create game."
        ConnectionState.WAITING_FOR_GUEST -> "Waiting for guest player."
        ConnectionState.WAITING_FOR_JOIN_CONFIRMATION -> "Connecting to game on server."
        ConnectionState.WAITING_FOR_INIT -> "Waiting for host player to start game"
        ConnectionState.PLAYING_MY_TURN -> "my turn"
        ConnectionState.WAITING_FOR_OPPONENT -> "waiting for opponent's turn"
    }