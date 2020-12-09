package cn.yz.xr.common.entity

import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.IntegerRedisMessage
import io.netty.handler.codec.redis.RedisMessage
import io.netty.handler.codec.redis.SimpleStringRedisMessage

class RString(
        var map: LinkedHashMap<String,String> = linkedMapOf(),
        var operationList: List<String> = listOf("SET","GET","SETNX","GETSET","STRLEN","APPEND","SETRANGE","INCR","GETRANGE","INCRBY","INCRBYFLOAT")
){
    fun set(key:String,value:String): RedisMessage{
        map[key] = value
        return SimpleStringRedisMessage("OK")
    }

    fun get(key:String): RedisMessage{
        return SimpleStringRedisMessage(map[key]?:"(nil)")
    }

    private fun setNX(key:String, value: String): RedisMessage{
        return if(map.containsKey(key)){
            IntegerRedisMessage(0)
        }else{
            map[key] = value
            SimpleStringRedisMessage("OK")
        }
    }

    private fun getSet(key:String, value: String): RedisMessage{
        var res = "(nil)"
        if(map.containsKey(key)){
            res = map[key]?:"(nil)"
            map[key] = value
        }else{
            map[key] = value
        }
        return SimpleStringRedisMessage(res)
    }

    private fun strLen(key:String): RedisMessage{
        val res = map[key]?:""
        return IntegerRedisMessage(res.length.toLong())
    }

    fun append(key:String, value: String): RedisMessage{
        map[key] = map[key]?:"" + value
        val res = map[key]?:""
        return IntegerRedisMessage(res.length.toLong())
    }

    fun setrange(key: String, offset:Int, value: String): RedisMessage{
        var res = map[key]?:""
        if(res.length < offset){
            for(i in res.length..offset){
                res += "\\x00"
            }
            res += offset
        }else if(offset + value.length < res.length){
            res = res.substring(0,offset) + value + res.substring(offset+value.length, res.length)
        }else{
            res = res.substring(0,offset) + value
        }
        return SimpleStringRedisMessage(res)
    }

    fun getRange(key:String, start:Int, end:Int): RedisMessage{
        if(end in 1 until start){
            return SimpleStringRedisMessage("")
        }
        var str = map[key]?:""
        var s = start
        var e = end
        if(end < 0){
            e = str.length + end + 1
        }
        if(start < 0){
            s = str.length + start
        }
        return SimpleStringRedisMessage(map[key]?:"".substring(s,e))
    }

    private fun incrBy(key: String,increment:Int): RedisMessage{
        var value = map[key]?:"0".toIntOrNull()
        return if(value is Int){
            map[key] = "${value+increment}"
            IntegerRedisMessage((value+increment).toLong())
        }else{
            ErrorRedisMessage("(error) ERR value is not an integer or out of range")
        }
    }

    private fun incrBrfFloat(key: String,increment:Float): RedisMessage{
        var value = map[key]?:"0.0".toFloatOrNull()
        return if(value is Float){
            map[key] = "${value+increment}"
            SimpleStringRedisMessage("${map[key]}")
        }else{
            ErrorRedisMessage("ERR value is not an float or out of range")

        }
    }

    fun keys():Set<String>{
        return map.keys
    }

    fun operation(command: String, array: List<String>): RedisMessage{
        return when(command){
            "GET" -> get(array[1])
            "SET" -> set(array[1], array[2])
            "SETNX" -> setNX(array[1], array[2])
            "GETSET" -> getSet(array[1], array[2])
            "STRLEN" -> strLen(array[1])
            "APPEND" -> append(array[1], array[2])
            "SETRANGE" -> setrange(array[1],array[2].toInt(),array[3])
            "INCR" -> incrBy(array[1],1)
            "GETRANGE" -> getRange(array[1],array[2].toInt(),array[3].toInt())
            "INCRBY" -> incrBy(array[1],array[2].toInt())
            "INCRBYFLOAT" -> incrBrfFloat(array[1],array[2].toFloat())
            "DECR" -> incrBy(array[1],-1)
            "DECRBY" -> incrBy(array[1], -1 * array[2].toInt())
            else -> ErrorRedisMessage("not supported command")
        }
    }
}