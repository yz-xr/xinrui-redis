package cn.yz.xr.common.entity

import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.RedisMessage

class RZset(
        var zSet: Any,
        var operationList: List<String> = listOf("ZADD")
) {
    fun operation(command: String, array: List<String>): RedisMessage {
        return ErrorRedisMessage("")
    }
}