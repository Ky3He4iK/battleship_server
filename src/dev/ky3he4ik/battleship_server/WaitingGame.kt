package dev.ky3he4ik.battleship_server

data class WaitingGame(val host: Client, val config: GameConfig, val password: String)
