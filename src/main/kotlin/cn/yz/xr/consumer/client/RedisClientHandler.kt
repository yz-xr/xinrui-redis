package cn.yz.xr.consumer.client

import io.netty.handler.codec.CodecException;
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.redis.*
import io.netty.util.CharsetUtil
import io.netty.util.ReferenceCountUtil

/**
 * @author abc
 */
class RedisClientHandler : ChannelDuplexHandler() {

    /**
     * 数据发出前调用此方法
     */
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val commands: List<String> = msg.toString().split("\\s+")
        val children: MutableList<RedisMessage> = ArrayList(commands.size)
        for (cmdStr: String in commands) {
            children.add(FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(ctx.alloc(), cmdStr)))
        }
        val request: RedisMessage = ArrayRedisMessage(children)
        ctx.write(request, promise)
    }

    /**
     * 数据回调
     */
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        val redisMessage = msg as RedisMessage
        printAggregatedRedisResponse(redisMessage)
        ReferenceCountUtil.release(redisMessage)
    }

    private fun printAggregatedRedisResponse(redisMessage: RedisMessage) {
        when(redisMessage) {
            is SimpleStringRedisMessage -> println(redisMessage.content())
            is ErrorRedisMessage -> println(redisMessage.content())
            is IntegerRedisMessage -> println(redisMessage.value())
            is FullBulkStringRedisMessage -> println(redisMessage)
            is ArrayRedisMessage -> {
                redisMessage.children().forEach { println(it.toString())}
            }
            else -> throw CodecException("unknown message type: $redisMessage")
        }
    }

    /**
     * 处理异常
     */
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    companion object {
        fun getString(redisMessage: FullBulkStringRedisMessage): String {
            if (redisMessage.isNull) {
                return "(null)"
            }
            return redisMessage.content().toString(CharsetUtil.UTF_8)
        }
    }
}