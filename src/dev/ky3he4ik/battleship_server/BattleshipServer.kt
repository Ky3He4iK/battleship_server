package dev.ky3he4ik.battleship_server

import java.io.IOException
import java.net.InetSocketAddress
import java.net.UnknownHostException

import org.java_websocket.WebSocket
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.StringBuilder
import java.net.InetAddress
import java.net.URI
import java.util.*
import kotlin.collections.HashMap

class BattleshipServer @Throws(UnknownHostException::class) constructor(port: Int) :
    WebSocketServer(InetSocketAddress(port)) {
    private val isDebug = java.lang.Boolean.getBoolean("debug")
    private val connectedClients = Collections.synchronizedMap(HashMap<String, Client>())
    private val games = Collections.synchronizedMap(HashMap<String, Game>())
    private val waitingGames = Collections.synchronizedMap(HashMap<String, WaitingGame>())
    private val pingClient: ClearingClient
    private var pendingReconnect = false

    init {
        pingClient = ClearingClient(URI("ws://localhost:$port"), this)
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
    }

    override fun onMessage(conn: WebSocket, message: String) {
        if (conn.remoteSocketAddress?.address?.address?.contentEquals(InetAddress.getLocalHost().address) == true
            || conn.remoteSocketAddress?.address?.address?.contentEquals(byteArrayOf(10, 8, 0, 2)) == true
            || conn.remoteSocketAddress?.address?.address?.contentEquals(InetAddress.getLoopbackAddress().address) == true
        ) {
            if (isDebug)
                println(message)
            if (message == "send_server_stats") {
                conn.send("${connectedClients.keys}\n${waitingGames.keys}\n${games.keys}")
                return
            }
            if (message == "clear") {
                games.forEach { (_, value) ->
                    if (!connectedClients.containsKey(value.p1.name) || !connectedClients.containsKey(value.p2.name)) {
                        games.remove(value.p1.name)
                        games.remove(value.p2.name)
                        value.finish()
                    }
                }
                waitingGames.forEach { (_, value) ->
                    if (!connectedClients.containsKey(value.host.name))
                        waitingGames.remove(value.host.name)
                }
                return
            }
            if (message == "clearFast") {
                connectedClients.forEach { (_, value) ->
                    if (!value.isAlive())
                        disconnect(value)
                }
                return
            }
        }
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
                        connectedClients[action.name] = Client(action.name, action.uuid, conn)
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
                Action.ActionType.CONNECTED, Action.ActionType.START_GAME -> null
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
//                            p1.game = game
//                            p2.game = game
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
                Action.ActionType.PLACE_SHIPS, Action.ActionType.TURN, Action.ActionType.SYNC -> {
                    if (isDebug)
                        println(message)

                    val game = games[action.name]
                    if (isDebug) {
                        println(action)
                        println(game)
                    }
                    if (game != null)
                        when (action.uuid) {
                            game.p1.uuid -> {
                                game.p2.connection.send(
                                    Action(
                                        action,
                                        playerId = 1,
                                        name = game.p2.name,
                                        otherName = action.name,
                                        uuid = game.p2.uuid,
                                        msg = action.msg
                                    ).toJson()
                                )
                                Action.ok()
                            }
                            game.p2.uuid -> {
                                game.p1.connection.send(
                                    Action(
                                        action,
                                        playerId = 0,
                                        name = game.p1.name,
                                        otherName = action.name,
                                        uuid = game.p1.uuid,
                                        msg = action.msg
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
                    val game = games[action.name]
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
                Action.ActionType.OK, Action.ActionType.NO -> return
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
        var counter = 0
        pingClient.connectBlocking()
        while (true) {
            try {
                Thread.sleep(1000) // Second between clear
                if (pendingReconnect) {
                    pingClient.reconnectBlocking()
                    pendingReconnect = false
                }
                pingClient.send("clearFast")
                if (counter > 60) { // full clean each minute
                    pingClient.send("clear")
                    counter = 0
                }
            } catch (e: InterruptedException) {
                println("Interrupt: ${e.message}; shutting down")
                break
            } catch (e: WebsocketNotConnectedException) {
                pingClient.reconnectBlocking()
            } catch (e: java.lang.Exception) {
                println(e.message)
                e.printStackTrace()
            }
        }
    }

    fun reconnect() {
        pendingReconnect = true
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
