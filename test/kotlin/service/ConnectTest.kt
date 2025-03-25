package service

import kotlin.test.Test
import kotlin.test.assertTrue

class ConnectTest {
    private var rootServiceHost = RootService()
    @Test
    fun `test initial connection`(){
        assertTrue { rootServiceHost.networkService.connection == ConnectionState.DISCONNECTED}

        assertTrue{rootServiceHost.networkService.connect("Testname")}
    }
}