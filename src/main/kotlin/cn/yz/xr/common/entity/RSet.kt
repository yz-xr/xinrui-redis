package cn.yz.xr.common.entity

import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.RedisMessage

class RSet(
    var rset: HashMap<String,Set<String>> = hashMapOf(),
    var operationList: List<String> = listOf("")
){

    fun operation(command: String, array: List<String>):RedisMessage{
        return ErrorRedisMessage("")
    }
}