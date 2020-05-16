package dev.ky3he4ik.battleship.logic

import com.google.gson.Gson
import dev.ky3he4ik.battleship.logic.GameConfig.Ship
import jdk.internal.jline.internal.Nullable

import java.util.ArrayList
import java.util.Arrays

import dev.ky3he4ik.battleship.ai.AILevel
import dev.ky3he4ik.battleship.utils.Constants

import jdk.nashorn.internal.runtime.Debug.id

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
    var aiLevel2: Int, @param:NotNull @field:NotNull
    @get:NotNull
    var gameType: GameType?, @param:NotNull @field:NotNull
    @get:NotNull
    var ships: ArrayList<Ship>?
) {
    var version: Int = 0

    class Ship(
        val length: Int, val id: Int, @param:NotNull @field:NotNull
        val name: String
    ) {

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o !is Ship) return false

            val ship = o as Ship?

            return if (length != ship!!.length) false else name == ship.name
        }

        override fun hashCode(): Int {
            var result = length
            result = 31 * result + id
            result = 31 * result + name.hashCode()
            return result
        }

        @NotNull
        fun convert(): World.Ship {
            return World.Ship(length, id, name, 0, 0, 0)
        }

        @NotNull
        fun clone(newId: Int): Ship {
            return Ship(length, newId, name)
        }

        @NotNull
        fun rotatedName(): String {
            return if (name.endsWith(Constants.ROTATED_SUFFIX)) name else name + Constants.ROTATED_SUFFIX
        }

        companion object {

            val sampleShipsWest: ArrayList<Ship>
                @NotNull
                get() = ArrayList(
                    Arrays.asList(
                        Ship(5, 1, Constants.SHIP_CARRIER_IMG),
                        Ship(4, 2, Constants.SHIP_BATTLESHIP_IMG),
                        Ship(3, 3, Constants.SHIP_SUBMARINE_IMG),
                        Ship(3, 4, Constants.SHIP_SUBMARINE_IMG),
                        Ship(2, 5, Constants.SHIP_PATROL_BOAT_IMG)
                    )
                )

            val sampleShipsEast: ArrayList<Ship>
                @NotNull
                get() = ArrayList(
                    Arrays.asList(
                        Ship(4, 1, Constants.SHIP_BATTLESHIP_IMG),
                        Ship(3, 2, Constants.SHIP_SUBMARINE_IMG),
                        Ship(3, 3, Constants.SHIP_SUBMARINE_IMG),
                        Ship(2, 4, Constants.SHIP_PATROL_BOAT_IMG),
                        Ship(2, 5, Constants.SHIP_PATROL_BOAT_IMG),
                        Ship(2, 6, Constants.SHIP_PATROL_BOAT_IMG),
                        Ship(1, 7, Constants.SHIP_RUBBER_BOAT_IMG),
                        Ship(1, 8, Constants.SHIP_RUBBER_BOAT_IMG),
                        Ship(1, 9, Constants.SHIP_RUBBER_BOAT_IMG),
                        Ship(1, 10, Constants.SHIP_RUBBER_BOAT_IMG)
                    )
                )

            val allShipsSamples: Array<Ship>
                @NotNull
                get() =
                    arrayOf(
                        Ship(5, 1, Constants.SHIP_CARRIER_IMG),
                        Ship(4, 2, Constants.SHIP_BATTLESHIP_IMG),
                        Ship(3, 3, Constants.SHIP_SUBMARINE_IMG),
                        Ship(2, 4, Constants.SHIP_PATROL_BOAT_IMG),
                        Ship(1, 5, Constants.SHIP_RUBBER_BOAT_IMG)
                    )
        }
    }

    enum class GameType {
        LOCAL_2P,
        AI,
        //        LOCAL_INET,
        //        BLUETOOTH,
        //        GLOBAL_INET,
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
        result = 31 * result + gameType!!.hashCode()
        result = 31 * result + ships!!.hashCode()
        return result
    }

    @NotNull
    fun toJSON(): String {
        return Gson().toJson(this)
    }

    @NotNull
    fun duplicate(@Nullable other: GameConfig?): GameConfig {
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

        val sampleConfigWest: GameConfig
            @NotNull
            get() = GameConfig(
                10, 10, false,
                true, false, 0, 1, AILevel.EASY.id, AILevel.NOVICE.id, GameType.AI, Ship.sampleShipsWest
            )

        val sampleConfigEast: GameConfig
            @NotNull
            get() = GameConfig(
                10, 10, false,
                true, false, 0, 1, AILevel.EASY.id, AILevel.NOVICE.id, GameType.AI, Ship.sampleShipsEast
            )

        val sampleMoving: GameConfig
            @NotNull
            get() = GameConfig(
                10, 10, true,
                true, false, -1, 1, AILevel.EASY.id, AILevel.EASY.id, GameType.AI, Ship.sampleShipsEast
            )

        @NotNull
        fun fromJSON(@NotNull json: String): GameConfig {
            return Gson().fromJson(json, GameConfig::class.java)
        }
    }
}
