package dev.ky3he4ik.battleship_server

import java.lang.StringBuilder
import java.net.InetSocketAddress

class Utils {
    companion object {
        fun ipToString(ip: InetSocketAddress): String {
            val sb = StringBuilder()
            for (b in ip.address.address) {
                if (b < 0)
                    sb.append(b + 256).append(".")
                else
                    sb.append(b).append(".")
            }
            return sb.substring(0, sb.length - 1)
        }
    }
}
