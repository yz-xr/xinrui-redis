package cn.yz.xr.common.entity

import cn.yz.xr.common.utils.MessageUtil
import io.netty.handler.codec.redis.*
import kotlin.math.absoluteValue

class RList(
        private var listMap: MutableMap<String, ArrayDeque<String>> = mutableMapOf(),
        var operationList: List<String> = listOf("LSET", "LPUSH", "LPOP", "RPUSH", "RPOP", "LLEN", "LINDEX", "LRANGE", "LREM")
) {

    private fun lpush(array: List<String>): RedisMessage {

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

    private fun rpush(array: List<String>): RedisMessage {

        if (listMap.containsKey(array[1])) {
            val exitedDeque: ArrayDeque<String>? = listMap[array[1]]
            if (exitedDeque != null) {
                for (i in 2 until array.size) {
                    exitedDeque.addLast(array[i])
                }
            } else {
                return ErrorRedisMessage("Inner exception")
            }
        } else {
            val deque: ArrayDeque<String> = ArrayDeque<String>()
            for (i in 2 until array.size) {
                deque.addLast(array[i])
            }
            listMap[array[1]] = deque
        }
        return IntegerRedisMessage((array.size - 2).toLong())
    }


    private fun lpop(array: List<String>): RedisMessage {

        if (listMap.containsKey(array[1])) {
            val existedDeque = listMap[array[1]]
            return if (existedDeque != null) {
                val removeEle: String? = existedDeque.removeFirstOrNull()
                SimpleStringRedisMessage(removeEle)
            } else {
                ErrorRedisMessage("Inner exception")
            }
        }
        return SimpleStringRedisMessage("(nil)")
    }

    private fun rpop(array: List<String>): RedisMessage {

        if (listMap.containsKey(array[1])) {
            val existedDeque = listMap[array[1]]
            return if (existedDeque != null) {
                val removeEle: String? = existedDeque.removeLastOrNull()
                // 是否存在问题？
                SimpleStringRedisMessage(removeEle)
            } else {
                ErrorRedisMessage("Inner exception")
            }
        }
        return SimpleStringRedisMessage("(nil)")
    }

    private fun lset(array: List<String>): RedisMessage {
        // 参数数目校验
        // return null 能不能过？
        MessageUtil.checkArgsNum(array, 4)

        return if (listMap.containsKey(array[1])) {
            val existedDeque = listMap[array[1]]
            val index = array[2].toInt()
            val value = array[3]
            if (existedDeque != null) {
                existedDeque[index] = value
                //val removeEle: String? = existedDeque.removeFirstOrNull()
                // 返回的是个啥？
                IntegerRedisMessage(1)
                //SimpleStringRedisMessage(removeEle)
            } else {
                ErrorRedisMessage("Inner exception")
            }
        } else {
            ErrorRedisMessage("ERR no such key")
        }
    }

    private fun llen(array: List<String>): RedisMessage {

        return if (listMap.containsKey(array[1])) {
            val existedDeque = listMap[array[1]]
            if (existedDeque != null) {
                IntegerRedisMessage(existedDeque.size.toLong())
                //SimpleStringRedisMessage(removeEle)
            } else {
                ErrorRedisMessage("Inner exception")
            }
        } else {
            IntegerRedisMessage(0)
        }
    }

    private fun llindex(array: List<String>): RedisMessage {
        // 校验参数个数
        MessageUtil.checkArgsNum(array, 3)

        return if (listMap.containsKey(array[1])) {
            val existedDeque = listMap[array[1]]
            if (MessageUtil.checkString2Int(array[2]) == null) {
                ErrorRedisMessage("ERR value is not an integer or out of range")
            } else {
                if (existedDeque != null) {
                    val index = array[2].toInt()
                    if (index.absoluteValue > existedDeque.size) {
                        SimpleStringRedisMessage("(nil)") // 索引超过
                    } else {
                        SimpleStringRedisMessage(existedDeque[index])
                    }
                } else {
                    ErrorRedisMessage("Inner exception")
                }
            }
        } else {
            IntegerRedisMessage(0)
        }
    }

    private fun lrange(array: List<String>): RedisMessage {
        // 校验参数个数
        MessageUtil.checkArgsNum(array, 4)

        return if (listMap.containsKey(array[1])) {
            val existedDeque = listMap[array[1]]
            if (existedDeque != null) {
                if (MessageUtil.checkString2Int(array[2]) == null
                        || MessageUtil.checkString2Int(array[3]) == null) {
                    ErrorRedisMessage("ERR value is not an integer or out of range")
                } else {
                    var low = array[2].toInt()
                    var high = array[3].toInt()
                    if (low < 0) low = 0
                    if (high > existedDeque.size - 1) high = existedDeque.size - 1
                    val subList = existedDeque.subList(low, high)
                    val rmList = mutableListOf<RedisMessage>()
                    subList.forEach { rmList.add(SimpleStringRedisMessage(it)) }
                    ArrayRedisMessage(rmList)
                }
            } else {
                ErrorRedisMessage("Inner exception")
            }
        } else {
            SimpleStringRedisMessage("(empty list or set)")
        }
    }

    private fun lrem(array: List<String>): RedisMessage {
        // 校验参数个数
        MessageUtil.checkArgsNum(array, 4)

        return if (listMap.containsKey(array[1])) {
            val existedDeque = listMap[array[1]]
            if (MessageUtil.checkString2Int(array[2]) == null) {
                ErrorRedisMessage("ERR value is not an integer or out of range")
            } else {
                val count = array[2].toInt()
                if (existedDeque != null) {
                    if (count.absoluteValue > 0) {
                        // 删除指定个数匹配的key
                        existedDeque.removeIf { it == array[3]}
                        IntegerRedisMessage(1)
                    } else {
                        // 当为0时删除所有的匹配的key
                        existedDeque.removeIf { it == array[3]}
                        IntegerRedisMessage(1)
                    }
                } else {
                    ErrorRedisMessage("Inner exception")
                }
            }
        } else {
            IntegerRedisMessage(0)
        }

    }

    fun operation(command: String, array: List<String>): RedisMessage {
        return when (command) {
            "LSET" -> lset(array)
            "LPUSH" -> lpush(array)
            "LPOP" -> lpop(array)
            "RPUSH" -> rpush(array)
            "RPOP" -> rpop(array)
            "LLEN" -> llen(array)
            "LINDEX" -> lpush(array)
            "LRANGE" -> lrange(array)
            "LREM" -> lpush(array)
            else -> ErrorRedisMessage("not support this command at present")
        }
    }
}