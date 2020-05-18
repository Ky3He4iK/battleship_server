package dev.ky3he4ik.battleship_server

import org.java_websocket.WebSocket
import kotlin.random.Random

public class Game(var p1: Client, var p2: Client) {
    val startTime = System.currentTimeMillis()
    val id = Random.nextLong()


    fun finish() {
        p1.connection.send(Action(Action.ActionType.DISCONNECT, 0, p1.name, id, 0, p1.uuid).toJson())
        p2.connection.send(Action(Action.ActionType.DISCONNECT, 1, p2.name, id, 0, p2.uuid).toJson())
    }
}