package dev.ky3he4ik.battleship_server

import org.java_websocket.WebSocket
import kotlin.random.Random

public class Game(var p1: Pair<Client, WebSocket>, var p2:  Pair<Client, WebSocket>) {
    val startTime = System.currentTimeMillis()
    val id = Random.nextLong()

    fun finish() {
        p1.second.send(Action(Action.ActionType.DISCONNECT, 0, p1.first.name, id, 0, p1.first.uuid).toJson())
        p2.second.send(Action(Action.ActionType.DISCONNECT, 1, p2.first.name, id, 0, p2.first.uuid).toJson())
    }
}