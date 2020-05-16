import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

public class BattleshipServer : WebSocketServer {
    private val connectedClients: ArrayList<String> = ArrayList()
    private val games = HashMap<String, Game>()

    @Throws(UnknownHostException::class)
    constructor(port: Int) : super(InetSocketAddress(port)) {
    }

    constructor(address: InetSocketAddress) : super(address) {}

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        conn.send("Welcome to the server!") //This method sends a message to the new client
        broadcast("new connection: " + handshake.resourceDescriptor) //This method sends a message to all clients connected
        println(conn.remoteSocketAddress.address.hostAddress + " entered the room!")
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        broadcast("$conn has left the room!")
        println("$conn has left the room!")
    }

    override fun onMessage(conn: WebSocket, message: String) {
        broadcast(message)
        println("$conn: $message")
    }

    override fun onMessage(conn: WebSocket?, message: ByteBuffer) {
        broadcast(message.array())
        println(conn.toString() + ": " + message)
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        ex.printStackTrace()
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    override fun onStart() {
        println("Server started!")
        connectionLostTimeout = 0
        connectionLostTimeout = 100
    }

    companion object {
        @Throws(InterruptedException::class, IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            var port = 6683 // 'bS' key codes
            try {
                port = Integer.parseInt(args[0])
            } catch (ex: Exception) {
            }

            val s = BattleshipServer(port)
            s.start()
            println("ChatServer started on port: " + s.port)

            val sysin = BufferedReader(InputStreamReader(System.`in`))
            while (true) {
                val input = sysin.readLine()
                s.broadcast(input)
                if (input == "exit") {
                    s.stop(1000)
                    break
                }
            }
        }
    }

}