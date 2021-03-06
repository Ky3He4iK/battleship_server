package dev.ky3he4ik.battleship_server

import kotlin.random.Random

class Game(var p1: Client, var p2: Client) {
    val id = Random.nextLong()

    fun finish() {
        if (p1.connection.isOpen)
            p1.connection.send(
                Action(
                    actionType = Action.ActionType.DISCONNECT,
                    playerId = 0,
                    name = p1.clientInfo.name,
                    gameId = id,
                    uuid = p1.clientInfo.uuid
                ).toJson()
            )
        if (p2.connection.isOpen)
            p2.connection.send(
                Action(
                    actionType = Action.ActionType.DISCONNECT,
                    playerId = 1,
                    name = p2.clientInfo.name,
                    gameId = id,
                    uuid = p2.clientInfo.uuid
                ).toJson()
            )
    }
}