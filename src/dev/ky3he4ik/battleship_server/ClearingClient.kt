package dev.ky3he4ik.battleship_server

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class ClearingClient(serverURI: URI, val callback: BattleshipServer) : WebSocketClient(serverURI) {

    override fun onOpen(handshakeData: ServerHandshake) {
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
    }

    override fun onMessage(message: String) {
    }

    override fun onError(ex: Exception) {
        callback.reconnect();
    }
}
