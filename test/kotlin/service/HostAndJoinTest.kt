package service

import entity.*
import kotlin.test.*

/**
 * Class that provides tests for hosting a new game or joining an existing game.
 * Might fail if server is offline.
 */
class HostAndJoinTest {
    private lateinit var rootServiceHost: RootService
    private lateinit var rootServiceGuest: RootService

    /**
     * Initializes two connections and the host hosts a game and a player joins.
     */
    private fun initConnections(){
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        rootServiceHost.networkService.hostGame("Horst", PlayerType.LOCAL)
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)

        val id = rootServiceHost.networkService.client?.sessionID
        if (id != null) {
            rootServiceGuest.networkService.joinGame(id, "Guestav")
        }
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)
    }

    /**
     * After initializing both connections, test that the host correctly created a lobby
     * and the guest correctly joined the game.
     */
//    @Test
//    fun `test that host can host game and others can join`(){
//        initConnections()
//
//        assertEquals(rootServiceHost.networkService.connection, ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
//        assertEquals(rootServiceGuest.networkService.connection, ConnectionState.WAITING_FOR_INIT)
//    }

    /**
     * Tests that a neither the player nor host can join or host a new game.
     * Makes sure, that attempting to create another local game also fails.
     */
//    @Test
//    fun `test that a player cannot join two games at the same time`(){
//        initConnections()
//
//        assertFails { rootServiceGuest.networkService.connect("testname")}
//        assertFails { rootServiceGuest.networkService.hostGame("testname",  PlayerType.LOCAL)}
//        assertFails { rootServiceGuest.gameService.startGame(gameConfig = GameConfig())}
//
//        assertFails { rootServiceHost.networkService.connect("testname")}
//        assertFails { rootServiceHost.networkService.connect("testname")}
//        assertFails { rootServiceHost.networkService.hostGame("testname",  PlayerType.LOCAL)}
//        assertFails { rootServiceHost.gameService.startGame(gameConfig = GameConfig())}
//    }

    private fun RootService.waitForState(state: ConnectionState, timeout: Int = 5000) {
        var timePassed = 0
        while (timePassed < timeout) {
            if (networkService.connection == state)
                return
            else {
                Thread.sleep(100)
                timePassed += 100
            }
        }
        error("Did not arrive at state $state after waiting $timeout ms")
    }

}