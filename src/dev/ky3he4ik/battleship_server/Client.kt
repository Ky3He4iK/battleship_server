package dev.ky3he4ik.battleship_server

import org.java_websocket.WebSocket

data class Client(val name: String, val uuid: Long, var connection: WebSocket, var game: Game?) {
    private var lastAccess = System.currentTimeMillis()

    fun isAlive() = System.currentTimeMillis() - lastAccess < 10_000 // 10 seconds before disconnect

    fun update() {
        lastAccess = System.currentTimeMillis()
    }
}
