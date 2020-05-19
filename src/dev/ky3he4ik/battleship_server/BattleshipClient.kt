package dev.ky3he4ik.battleship_server

import org.java_websocket.client.WebSocketClient
import java.net.URISyntaxException
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class BattleshipClient(serverURI: URI, val name: String) : WebSocketClient(serverURI) {

    override fun onOpen(handshakeData: ServerHandshake) {
        send("Server connected")
        println("new connection opened")
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        println("closed with exit code $code additional info: $reason")
    }

    override fun onMessage(message: String) {
        println("received message: $message")
    }

    override fun onError(ex: Exception) {
        System.err.println("an error occurred:$ex")
    }

    companion object {
        @Throws(URISyntaxException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val client = BattleshipClient(URI("ws://localhost:8887"), "Client#1")
            client.connect()
        }
    }
}
