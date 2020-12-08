package cn.yz.xr.common

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.redis.ArrayRedisMessage



/**
 * Redis server 与 akka 交互数据结构
 *
 * command : 命令
 * kv ：键值map
 * socketChannel : netty通道
 */
data class RMessage(
        val command: String,
        val content: ArrayRedisMessage,
        //val kv: HashMap<String, Any>,
        val channel: ChannelHandlerContext,
        val timeStamp: Long = System.currentTimeMillis()
)