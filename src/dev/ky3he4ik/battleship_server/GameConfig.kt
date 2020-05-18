package dev.ky3he4ik.battleship_server

import com.google.gson.Gson

import java.util.ArrayList

class GameConfig

/**
 * @param width           - width of field (in cells). Default 10
 * @param height          - height of field (in cells). Default 10
 * @param movingEnabled   - is ships can be moved after each turn?
 * @param additionalShots - if true player will get one more shot after enemy's ship hit
 * @param decreasingField - if true field will eventually shrink
 * @param movingPerTurn   - how many ships an be moved each turn
 * @param shotsPerTurn    - how many shoots each player gets
 * @param aiLevel         - difficulty level (only GameType.AI)
 * @param gameType        - type of game
 * @param ships           - list of available ships
 */
    (
    var width: Int,
    var height: Int,
    var isMovingEnabled: Boolean,
    var isAdditionalShots: Boolean,
    var isDecreasingField: Boolean,
    var movingPerTurn: Int,
    var shotsPerTurn: Int,
    var aiLevel: Int,
    var aiLevel2: Int,
    var gameType: GameType,
    var ships: ArrayList<Ship>
) {
    var version: Int = 0

    data class Ship(
        val length: Int, val id: Int,
        val name: String
    ) {

        override fun hashCode(): Int {
            var result = length
            result = 31 * result + id
            result = 31 * result + name.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Ship

            if (length != other.length) return false
            if (id != other.id) return false
            if (name != other.name) return false

            return true
        }
    }

    enum class GameType {
        LOCAL_2P,
        AI,
        LOCAL_INET,
        BLUETOOTH,
        GLOBAL_INET,
        AI_VS_AI
    }

    init {
        this.version = 1
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is GameConfig) return false

        val config = o as GameConfig?

        if (width != config!!.width) return false
        if (height != config.height) return false
        if (isMovingEnabled != config.isMovingEnabled) return false
        if (isAdditionalShots != config.isAdditionalShots) return false
        if (isDecreasingField != config.isDecreasingField) return false
        if (movingPerTurn != config.movingPerTurn) return false
        if (shotsPerTurn != config.shotsPerTurn) return false
        if (aiLevel != config.aiLevel) return false
        if (aiLevel2 != config.aiLevel2) return false
        if (version != config.version) return false
        return if (gameType != config.gameType) false else ships == config.ships
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + if (isMovingEnabled) 1 else 0
        result = 31 * result + if (isAdditionalShots) 1 else 0
        result = 31 * result + if (isDecreasingField) 1 else 0
        result = 31 * result + movingPerTurn
        result = 31 * result + shotsPerTurn
        result = 31 * result + aiLevel
        result = 31 * result + aiLevel2
        result = 31 * result + version
        result = 31 * result + gameType.hashCode()
        result = 31 * result + ships.hashCode()
        return result
    }

    fun toJSON(): String {
        return Gson().toJson(this)
    }

    fun duplicate(other: GameConfig?): GameConfig {
        var other = other
        if (other == null) {
            other = GameConfig(
                width,
                height,
                isMovingEnabled,
                isAdditionalShots,
                isDecreasingField,
                movingPerTurn,
                shotsPerTurn,
                aiLevel,
                aiLevel2,
                gameType,
                ships
            )
            other.version = version
        } else {
            other.width = width
            other.height = height
            other.isMovingEnabled = isMovingEnabled
            other.isAdditionalShots = isAdditionalShots
            other.isDecreasingField = isDecreasingField
            other.movingPerTurn = movingPerTurn
            other.shotsPerTurn = shotsPerTurn
            other.aiLevel = aiLevel
            other.aiLevel2 = aiLevel2
            other.version = version
            other.gameType = gameType
            other.ships = ships
        }
        return other
    }

    companion object {
        fun fromJSON(json: String): GameConfig {
            return Gson().fromJson(json, GameConfig::class.java)
        }
    }
}

