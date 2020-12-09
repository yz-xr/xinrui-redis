package cn.yz.xr.common.entity


import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.RedisMessage
import io.netty.handler.codec.redis.SimpleStringRedisMessage

class RCommon(
        var operationList:List<String> = listOf("KEYS","MSET","MGET","SETNX")
){
    private fun keys(): RedisMessage{
        return SimpleStringRedisMessage("")
    }

    fun operation(command: String, array: List<String>): RedisMessage {
        return when(command){
            "KEYS" -> keys()
            else -> ErrorRedisMessage("not supported command")
        }
    }
}