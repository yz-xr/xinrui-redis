package cn.yz.xr.common.entity

import cn.yz.xr.common.utils.MessageUtil
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.redis.*

class RList(
        var listMap: MutableMap<String, ArrayDeque<String>> = mutableMapOf(),
        var operationList: List<String> = listOf("LSET", "LPUSH", "LPOP", "RPUSH", "RPOP", "LLEN", "LINDEX", "LREM")
) {

    fun lpush(array: List<String>): RedisMessage {

        if (listMap.containsKey(array[1])) {
            val exitedDeque: ArrayDeque<String>? = listMap[array[1]]
            if (exitedDeque != null) {
                for (i in 2 until array.size) {
                    exitedDeque.addFirst(array[i])
                }
            } else {
                return ErrorRedisMessage("Inner exception")
            }
        } else {
            val deque: ArrayDeque<String> = ArrayDeque<String>()
            for (i in 2 until array.size) {
                deque.addFirst(array[i])
            }
            listMap[array[1]] = deque
        }

        return IntegerRedisMessage((array.size - 2).toLong())
    }

    fun lpop(array: List<String>): RedisMessage {
        // 校验参数个数
        //MessageUtil.checkArgsNum(array, 2)

        if (listMap.containsKey(array[1])) {
            val existedDeque = listMap[array[1]]
            if (existedDeque != null) {
                val removeEle:String? = existedDeque.removeFirst()
                if (removeEle != null) {
                    return SimpleStringRedisMessage(removeEle)
                } else {
                    return ErrorRedisMessage("Inner exception")
                }
            } else {
                return ErrorRedisMessage("Inner exception")
            }
        }
        return SimpleStringRedisMessage("(nil)")
    }

    fun operation(command: String, array: List<String>): RedisMessage {
        return when (command) {
            "LSET" -> lpush(array)
            "LPUSH" -> lpush(array)
            "LPOP" -> lpop(array)
            "RPUSH" -> lpush(array)
            "RPOP" -> lpush(array)
            "LLEN" -> lpush(array)
            "LINDEX" -> lpush(array)
            else -> ErrorRedisMessage("not support this command at present")
        }
    }
}