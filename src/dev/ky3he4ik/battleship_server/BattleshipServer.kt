package dev.ky3he4ik.battleship_server

import java.io.IOException
import java.net.InetSocketAddress
import java.net.UnknownHostException

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap

public class BattleshipServer @Throws(UnknownHostException::class) constructor(port: Int) :
    WebSocketServer(InetSocketAddress(port)) {
    private val connectedClients = Collections.synchronizedMap(HashMap<String, Client>())
    private val games = Collections.synchronizedMap(HashMap<String, Game>())

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
//        conn.send("Welcome to the server!") //This method sends a message to the new client
//        broadcast("new connection: " + handshake.resourceDescriptor) //This method sends a message to all clients connected
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
//        broadcast("$conn has left the room!")
    }

    override fun onMessage(conn: WebSocket, message: String) {
//        broadcast(message)
        val action = Action.fromJson(message)
        if (connectedClients.containsKey(action.name)) {
            val client = connectedClients[action.name]
            if (client?.uuid == action.uuid) {
                client.update()
                client.connection = conn
            } else
                return
        }
        val ans: Action? =
            when (action.actionType) {
                Action.ActionType.CONNECT -> {
                    if (connectedClients.containsKey(action.name))
                        null
                    else {
                        connectedClients[action.name] = Client(action.name, action.uuid, conn, null)
                        action
                    }
                }
                Action.ActionType.GET_HOSTS -> {
                    val hosts = StringBuilder()
                    games.keys.forEach { host -> hosts.append(host).append('\n') }
                    Action(
                        Action.ActionType.CONNECT,
                        null,
                        action.playerId,
                        action.name,
                        null,
                        null,
                        null,
                        hosts.toString(),
                        action.gameId,
                        1,
                        action.uuid
                    )
                }
                Action.ActionType.HOST -> {
                    if (games.containsKey(action.name)) {
                        null
                    } else {

                        null
                    }
                }
                Action.ActionType.CONNECTED -> {
                    null
                }
                Action.ActionType.INFO -> {
                    null
                }
                Action.ActionType.JOIN -> {
                    null
                }
                Action.ActionType.SPECTATE -> {
                    null
                }
                Action.ActionType.START_GAME -> {
                    null
                }
                Action.ActionType.PLACE_SHIPS -> {
                    null
                }
                Action.ActionType.TURN -> {
                    null
                }
                Action.ActionType.GAME_END -> {
                    null
                }
                Action.ActionType.DISCONNECT -> {
                    null
                }
                Action.ActionType.PING -> action
                Action.ActionType.OK -> null
                else -> {
                    System.err.println("Unknown action type: ${action.actionType}")
                    null
                }
            }
        if (ans != null)
            conn.send(ans.toJson())
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        ex.printStackTrace()
        println("Error: ${ex.message} on $conn")
    }

    override fun onStart() {
        println("Server started!")
        connectionLostTimeout = 0
        connectionLostTimeout = 100
    }

    fun working() {
        while (true) {
            try {
                connectedClients.forEach { (key, value) ->
                    if (!value.isAlive()) {
                        connectedClients.remove(key)
                        games[key]?.finish()
                        games.remove(key)
                    }
                }
                Thread.sleep(1000); // Second between clear
            } catch (e: InterruptedException) {
                println("Interruped: ${e.message}; shutting down")
            } catch (e: java.lang.Exception) {
                println(e.message)
                e.printStackTrace()
            }
        }
    }

    companion object {
        @Throws(InterruptedException::class, IOException::class)
        @JvmStatic
        public fun main(args: Array<String>) {
            val server = BattleshipServer(6683) // 'bS' key codes
            server.start()
            println("BattleshipServer started on port: " + server.port)
            server.working();
        }
    }
}
