import com.google.gson.Gson
import dev.ky3he4ik.battleship.logic.GameConfig

public data class Action(
    val actionType: ActionType,
    val config: GameConfig?,
    val playerId: Int,
    val name: String,
    val pos: IntArray?,
    val ships: Array<IntArray>?, /* id, idx, idy, rotation */
    val otherName: String?,
    val msg: String?,
    val gameId: Long,
    val code: Int
) {
    public enum class ActionType {
        CONNECT, // < name
        // > -

        SEARCH, // < name, otherName
        // > msg: `\n` separated list of connected names

        HOST, // < name, config, msg: password
        // > code

        CONNECTED, // > otherName

        INFO, // < otherName
        // > config, code: 1 if with password else 0

        JOIN, // < name, otherName, msg: password?
        // > code: zero if fail, config

        SPECTATE, // name, otherName, msg: password?
        // > code: zero if fail, config

        START_GAME, // > playerId


        PLACE_SHIPS,    // < code, playerId, ships
                        // > -//-


        TURN,   // < code, playerId, ships
                // > -//-


        GAME_END, // > -

        DISCONNECT, // < -
        // > -
    }

    public fun toJson(): String {
        return Gson().toJson(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Action

        if (actionType != other.actionType) return false
        if (config != other.config) return false
        if (playerId != other.playerId) return false
        if (name != other.name) return false
        if (pos != null) {
            if (other.pos == null) return false
            if (!pos.contentEquals(other.pos)) return false
        } else if (other.pos != null) return false
        if (ships != null) {
            if (other.ships == null) return false
            if (!ships.contentDeepEquals(other.ships)) return false
        } else if (other.ships != null) return false
        if (otherName != other.otherName) return false
        if (msg != other.msg) return false

        return true
    }

    override fun hashCode(): Int {
        var result = actionType.hashCode()
        result = 31 * result + (config?.hashCode() ?: 0)
        result = 31 * result + playerId
        result = 31 * result + name.hashCode()
        result = 31 * result + (pos?.contentHashCode() ?: 0)
        result = 31 * result + (ships?.contentDeepHashCode() ?: 0)
        result = 31 * result + (otherName?.hashCode() ?: 0)
        result = 31 * result + (msg?.hashCode() ?: 0)
        return result
    }


    companion object {
        @JvmStatic
        public fun fromJson(json: String): Action {
            return Gson().fromJson(json, Action::class.java)
        }
    }
}
