package dev.ky3he4ik.battleship_server

import com.google.gson.Gson

data class Action(
    val actionType: ActionType,
    val config: String? = null,
    val playerId: Int = 0,
    val name: String,
    val pos: IntArray? = null,
    val ships: Array<IntArray>? = null, /* id, idx, idy, rotation */
    val otherName: String? = null,
    val msg: String? = null,
    val gameId: Long = 0,
    val code: Int = 0,
    val uuid: Long
) {
    enum class ActionType { // name, uuid for all
        CONNECT, // < -
        // > if success

        GET_HOSTS, // < name, otherName
        // > msg: `\n` separated list of connected names

        HOST, // < name, config, msg: password
        // > code

        CONNECTED, // > otherName

        INFO, // < otherName
        // > config, code: password length

        JOIN, // < name, otherName, msg: password?
        // > code: zero if fail, config

        START_GAME, // > playerId


        PLACE_SHIPS,    // < code, playerId, ships
        // > -//-


        TURN,   // < code, playerId, ships
        // > -//-


        GAME_END, // > -

        DISCONNECT, // < -
        // > -

        PING,   // < -
        // > -

        OK,     // < -
        // > -

        // Just... no
        NO,     // < -
        // > -

        SYNC, // msg: my World in json
    }

    constructor(
        actionType: ActionType,
        client: Client
    ) : this(actionType, name = client.name, uuid = client.uuid)

    constructor(
        action: Action,
        actionType: ActionType = action.actionType,
        config: String? = action.config,
        playerId: Int = action.playerId,
         name: String = action.name,
         pos: IntArray? = action.pos,
         ships: Array<IntArray>? = action.ships, /* id, idx, idy, rotation */
         otherName: String? = action.otherName,
         msg: String? = action.msg,
         gameId: Long = action.gameId,
         code: Int = action.code,
         uuid: Long = action.uuid
    ) : this(actionType, config, playerId, name, pos, ships, otherName, msg, gameId, code, uuid)

    fun toJson(): String = Gson().toJson(this)

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
        fun fromJson(json: String): Action? = Gson().fromJson(json, Action::class.java)

        @JvmStatic
        fun no() = Action(ActionType.NO, name = "", uuid = 0)

        @JvmStatic
        fun ok() = Action(ActionType.OK, name = "", uuid = 0)
    }
}
