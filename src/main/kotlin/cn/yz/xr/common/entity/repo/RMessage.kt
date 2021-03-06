package cn.yz.xr.common.entity.repo

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.redis.ArrayRedisMessage

/**
 * Redis server 与 akka 交互数据结构
 * @author lewy
 *
 * command : 命令
 * key : 键值
 * content ：ArrayRedisMessage
 * socketChannel : netty通道
 * timeStamp : System.currentTimeMillis()
 */
data class RMessage(
        val command: String,
        val Key: String,
        val content: ArrayRedisMessage,
        val channel: ChannelHandlerContext,
        val timeStamp: Long = System.currentTimeMillis()
)