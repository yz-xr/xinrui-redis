package cn.yz.xr.consumer.server

import cn.hutool.core.util.CharsetUtil
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.FullBulkStringRedisMessage
import io.netty.handler.codec.redis.RedisMessage

class RedisServerHandler : ChannelDuplexHandler() {

    private val stringMap: MutableMap<Any, Any> = HashMap()

    /**
     * 处理收到的消息
     */
    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        println("服务器收到的消息：$msg")
        val message: ArrayRedisMessage = msg as ArrayRedisMessage
        val response = printAggregatedRedisResponseRequest(message)
        // val response2 = invokeAkka(message)

        println("服务器返回结果：${response}")
        val fullBulkStringRedisMessage = FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(ctx.alloc(), response.toString()))
        ctx.writeAndFlush(fullBulkStringRedisMessage)
    }

    /**
     * 调用 Akka
     */
    private fun invokeAkka(message: ArrayRedisMessage): Any {
        return ""
    }

    /**
     * 自定义实现逻辑（不使用）
     */
    private fun printAggregatedRedisResponseRequest(message: ArrayRedisMessage): Any {
        when ((message.children()[0] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)) {
            "set" -> {
                stringMap[(message.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)] =
                        (message.children()[2] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)
                return "ok"
            }
            "get" -> {
                val res = stringMap[(message.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)]
                return res ?: "(null)"
            }
            "del" -> {
                stringMap.remove((message.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8))
                return "ok"
            }
        }
        return "(null)"
    }

    /**
     * 处理异常情况
     */
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        super.write(ctx, msg, promise)
    }

    /**
     * 首次建立连接时返回消息
     */
    override fun channelActive(ctx: ChannelHandlerContext) {
        println("连接的客户端地址：${ctx.channel().remoteAddress()}")
        super.channelActive(ctx)
    }

}