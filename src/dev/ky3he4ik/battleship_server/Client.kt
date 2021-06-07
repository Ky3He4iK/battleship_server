package dev.ky3he4ik.battleship_server

import br.com.devsrsouza.redissed.RedisObject
import br.com.devsrsouza.redissed.RedissedCommands
import org.java_websocket.WebSocket
import java.net.InetAddress

class Client(name: String, uuid: Long, var connection: WebSocket, database: String, commands: RedissedCommands)  {
    var lastAccess = System.currentTimeMillis()

    class ClientInfo(name: String, uuid: Long, database: String, commands: RedissedCommands) : RedisObject(database, commands) {
        val name: String by string(name)
        val uuid: Long by long(uuid)
        var isAdmin: Boolean by boolean(false)
    }

    val clientInfo = ClientInfo(name, uuid, "$database:$name", commands)

    init {
        if (name == "admin") {
            for (addr in adminWhitelist)
                if (connection.remoteSocketAddress.address.address?.contentEquals(addr) == true)
                    clientInfo.isAdmin = true
        }
    }

    fun isAlive() = System.currentTimeMillis() - lastAccess < 10_000 // 10 seconds before disconnect

    fun update() {
        lastAccess = System.currentTimeMillis()
    }

    companion object {
        val adminWhitelist = arrayListOf(
            byteArrayOf(127, 0, 0, 1),
            InetAddress.getLocalHost().address,
            InetAddress.getLoopbackAddress().address,
            byteArrayOf(193.toByte(), 38, 54, 97),
            byteArrayOf(10, 8, 0, 2)
        )
    }
}
