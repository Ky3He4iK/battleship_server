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

class BattleshipServer @Throws(UnknownHostException::class) constructor(port: Int) :
    WebSocketServer(InetSocketAddress(port)) {
    private val connectedClients = Collections.synchronizedMap(HashMap<String, Client>())
    private val games = Collections.synchronizedMap(HashMap<String, Game>())
    private val waitingGames = Collections.synchronizedMap(HashMap<String, WaitingGame>())

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
//        conn.send("Welcome to the server!") //This method sends a message to the new client
//        broadcast("new connection: " + handshake.resourceDescriptor) //This method sends a message to all clients connected
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
//        broadcast("$conn has left the room!")
    }

    override fun onMessage(conn: WebSocket, message: String) {
//        broadcast(message)
        val action = Action.fromJson(message) ?: return
        if (connectedClients.containsKey(action.name)) {
            val client = connectedClients[action.name]
            if (client?.uuid == action.uuid) {
                client.update()
                client.connection = conn
            } else {
                conn.send(Action.no().toJson())
                return
            }
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
                    waitingGames.keys.forEach { host -> hosts.append(host).append('\n') }
                    Action(action, msg = hosts.toString())
                }
                Action.ActionType.HOST -> {
                    if (games.containsKey(action.name) || waitingGames.containsKey(action.name))
                        null
                    else {
                        val client = connectedClients[action.name]
                        if (client != null && action.config != null && action.msg != null)
                            waitingGames[action.name] = WaitingGame(client, action.config, action.msg)
                        action
                    }
                }
                Action.ActionType.CONNECTED -> null
                Action.ActionType.INFO -> {
                    val game = waitingGames[action.otherName]
                    if (game != null)
                        Action(
                            action,
                            config = game.config,
                            code = game.password.length
                        )
                    else
                        null
                }
                Action.ActionType.JOIN -> {
                    val wGame = waitingGames[action.otherName]
                    if (wGame != null && wGame.password == action.msg) {
                        val p1 = connectedClients[action.otherName]
                        val p2 = connectedClients[action.name]
                        if (p1 != null && p2 != null) {
                            val game = Game(p1, p2)
                            games[p1.name] = game
                            games[p2.name] = game
                            waitingGames.remove(p1.name)
                            p1.connection.send(
                                Action(
                                    Action.ActionType.START_GAME,
                                    playerId = 0,
                                    name = p1.name,
                                    uuid = p1.uuid,
                                    gameId = game.id
                                ).toJson()
                            )
                            Action(
                                Action.ActionType.START_GAME,
                                playerId = 1,
                                name = p2.name,
                                uuid = p2.uuid,
                                gameId = game.id
                            )
                        } else
                            null
                    } else
                        null
                }
                Action.ActionType.START_GAME -> null
                Action.ActionType.PLACE_SHIPS, Action.ActionType.TURN -> {
                    val game = connectedClients[action.name]?.game
                    if (game != null)
                        when (action.uuid) {
                            game.p1.uuid -> {
                                game.p2.connection.send(
                                    Action(
                                        action,
                                        playerId = 1,
                                        name = action.otherName ?: "",
                                        otherName = action.name,
                                        uuid = game.p2.uuid
                                    ).toJson()
                                )
                                Action.ok()
                            }
                            game.p2.uuid -> {
                                game.p2.connection.send(
                                    Action(
                                        action,
                                        playerId = 0,
                                        name = action.otherName ?: "",
                                        otherName = action.name,
                                        uuid = game.p1.uuid
                                    ).toJson()
                                )
                                Action.ok()
                            }
                            else -> null
                        }
                    else
                        null
                }
                Action.ActionType.GAME_END -> {
                    val game = connectedClients[action.name]?.game
                    if (game != null) {
                        game.p1.connection.send(Action(action.actionType, game.p1).toJson())
                        game.p2.connection.send(Action(action.actionType, game.p2).toJson())
                        games.remove(game.p1.name)
                        games.remove(game.p2.name)
                        return
                    }
                    waitingGames.remove(action.name)
                    null
                }
                Action.ActionType.DISCONNECT -> {
                    disconnect(connectedClients[action.name])
                    return
                }
                Action.ActionType.PING -> action
                Action.ActionType.OK -> return
                Action.ActionType.NO -> return
                else -> {
                    System.err.println("Unknown action type: ${action.actionType}")
                    null
                }
            }
        if (ans != null)
            conn.send(ans.toJson())
        else
            conn.send(Action.no().toJson())
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

    private fun disconnect(client: Client?) {
        if (client != null) {
            connectedClients.remove(client.name)
            games[client.name]?.finish()
            val p1 = games[client.name]?.p1?.name
            val p2 = games[client.name]?.p2?.name
            games.remove(p1)
            games.remove(p2)
            waitingGames.remove(client.name)
        }
    }

    private fun working() {
        while (true) {
            try {
                connectedClients.forEach { (_, value) ->
                    if (!value.isAlive()) {
                        disconnect(value)
                    }
                }
                Thread.sleep(1000) // Second between clear
            } catch (e: InterruptedException) {
                println("Interrupt: ${e.message}; shutting down")
            } catch (e: java.lang.Exception) {
                println(e.message)
                e.printStackTrace()
            }
        }
    }

    companion object {
        @Throws(InterruptedException::class, IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val server = BattleshipServer(6683) // 'bS' key codes
            server.start()
            println("BattleshipServer started on port: " + server.port)
            server.working()
        }
    }
}
