package cn.yz.xr.common.entity

import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.IntegerRedisMessage
import io.netty.handler.codec.redis.RedisMessage
import io.netty.handler.codec.redis.SimpleStringRedisMessage

/**
 * author：雷克萨
 */

class RString(
        var map: HashMap<String, String> = hashMapOf(),
        var operationList: List<String> = listOf("SET", "GET", "SETNX", "GETSET", "STRLEN", "APPEND", "SETRANGE", "INCR", "GETRANGE", "INCRBY", "INCRBYFLOAT", "DECR", "DECRBY")
) {
    fun set(key: String, value: String): RedisMessage {
        map[key] = value
        return SimpleStringRedisMessage("OK")
    }

    fun get(key: String): RedisMessage {
        return SimpleStringRedisMessage(map[key] ?: "(nil)")
    }

    private fun setNX(key: String, value: String): RedisMessage {
        return if (map.containsKey(key)) {
            IntegerRedisMessage(0)
        } else {
            map[key] = value
            SimpleStringRedisMessage("OK")
        }
    }

    private fun getSet(key: String, value: String): RedisMessage {
        var res = "(nil)"
        if (map.containsKey(key)) {
            res = map[key] ?: "(nil)"
            map[key] = value
        } else {
            map[key] = value
        }
        return SimpleStringRedisMessage(res)
    }

    private fun strLen(key: String): RedisMessage {
        val res = map[key] ?: ""
        return IntegerRedisMessage(res.length.toLong())
    }

    private fun append(key: String, value: String): RedisMessage {
        this.map[key] = this.map[key] + value
        return IntegerRedisMessage(this.map[key]?.length?.toLong()!!)
    }

    private fun setrange(key: String, offset: Int, value: String): RedisMessage {
        var res = map[key] ?: ""
        if (res.length < offset) {
            for (i in res.length..offset) {
                res += "\\x00"
            }
            res += value
        } else if (offset + value.length < res.length) {
            res = res.substring(0, offset) + value + res.substring(offset + value.length, res.length)
        } else {
            res = res.substring(0, offset) + value
        }
        this.map[key] = res
        return SimpleStringRedisMessage(res)
    }

    private fun getRange(key: String, start: Int, end: Int): RedisMessage {
        if (end in 1 until start) {
            return SimpleStringRedisMessage("")
        }
        val str = map[key] ?: ""
        var s = start
        var e = end
        if (end < 0) {
            e = str.length + end + 1
        }
        if (start < 0) {
            s = str.length + start
        }
        return SimpleStringRedisMessage(map[key]?.substring(s, e))
    }

    private fun incrBy(key: String, increment: Int): RedisMessage {
        var value = 0
        if (map.containsKey(key)) {
            try {
                value = map[key]?.toInt() ?: return ErrorRedisMessage("ERR value is not an integer or out of range")
            } catch (e: Exception) {
                return ErrorRedisMessage("ERR value is not an integer or out of range")
            }
        }
        this.map[key] = "${value + increment}"
        return IntegerRedisMessage((value + increment).toLong())
    }

    private fun incrByFloat(key: String, increment: Float): RedisMessage {
        var value = 0.0f
        if (map.containsKey(key)) {
            try {
                value = map[key]?.toFloat() ?: return ErrorRedisMessage("ERR value is not an integer or out of range")
            } catch (e: Exception) {
                return ErrorRedisMessage("ERR value is not an integer or out of range")
            }
        }
        this.map[key] = "${value + increment}"
        return SimpleStringRedisMessage("${this.map[key]}")
    }

    fun operation(command: String, key: String, array: List<String>): RedisMessage {
        return when (command) {
            "GET" -> get(key)
            "SET" -> set(key, array[2])
            "SETNX" -> setNX(key, array[2])
            "GETSET" -> getSet(key, array[2])
            "STRLEN" -> strLen(key)
            "APPEND" -> append(key, array[2])
            "SETRANGE" -> setrange(key, array[2].toInt(), array[3])
            "INCR" -> incrBy(key, 1)
            "GETRANGE" -> getRange(key, array[2].toInt(), array[3].toInt())
            "INCRBY" -> incrBy(key, array[2].toInt())
            "INCRBYFLOAT" -> incrByFloat(key, array[2].toFloat())
            "DECR" -> incrBy(key, -1)
            "DECRBY" -> incrBy(key, -1 * array[2].toInt())
            else -> ErrorRedisMessage("not supported command")
        }
    }
}