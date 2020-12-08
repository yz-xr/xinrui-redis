package cn.yz.xr.common.entity.repo

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.redis.ArrayRedisMessage

/**
 * Redis server 与 akka 交互数据结构
 *
 * command : 命令
 * content ：ArrayRedisMessage
 * socketChannel : netty通道
 * timeStamp : System.currentTimeMillis()
 */
data class RMessage(
        val command: String,
        val content: ArrayRedisMessage,
        //val kv: HashMap<String, Any>,
        val channel: ChannelHandlerContext,
        val timeStamp: Long = System.currentTimeMillis()
)