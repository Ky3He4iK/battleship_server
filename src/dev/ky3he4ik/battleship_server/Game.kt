package dev.ky3he4ik.battleship_server

import kotlin.random.Random

public class Game(var p1: Client, var p2: Client) {
    val id = Random.nextLong()

    fun finish() {
        p1.connection.send(
            Action(
                actionType = Action.ActionType.DISCONNECT,
                playerId = 0,
                name = p1.name,
                gameId = id,
                uuid = p1.uuid
            ).toJson()
        )
        p2.connection.send(
            Action(
                actionType = Action.ActionType.DISCONNECT,
                playerId = 1,
                name = p2.name,
                gameId = id,
                uuid = p2.uuid
            ).toJson()
        )
    }
}