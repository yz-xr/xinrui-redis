package cn.yz.xr.consumer.server

import cn.hutool.core.util.CharsetUtil
import cn.yz.xr.ApplicationMain
import cn.yz.xr.common.entity.repo.RMessage
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.FullBulkStringRedisMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.Throws
import kotlin.collections.HashMap

class RedisServerHandler : ChannelDuplexHandler() {

    private val logger: Logger = LoggerFactory.getLogger(RedisServerHandler::class.java)

    // private val stringMap: ConcurrentHashMap<Any, Any> = ConcurrentHashMap()

    /**
     * 处理收到的消息
     */
    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        // logger.info("服务器收到的消息: {}", msg)
        //println("netty start time: ${System.currentTimeMillis()}")
        val message: ArrayRedisMessage = msg as ArrayRedisMessage
        // val response = printAggregatedRedisResponseRequest(message)
        invokeAkka(ctx, message)

         //logger.info("服务器返回结果: {}", response)
         //val fullBulkStringRedisMessage = FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(ctx.alloc(), response.toString()))
         //ctx.writeAndFlush(fullBulkStringRedisMessage)
    }

    /**
     * 调用 Akka
     */
    private fun invokeAkka(ctx: ChannelHandlerContext, message: ArrayRedisMessage) {
        val command = (message.children()[0] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)
        val key = (message.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)

        when (command) {
            "COMMAND" -> {
                val ret = "ok"
                val fullBulkStringRedisMessage = FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(ctx.alloc(), ret))
                ctx.writeAndFlush(fullBulkStringRedisMessage)
                logger.info("服务器收到COMMAND指令，返回结果: {}", ret)
            }
            else -> {
                logger.info("服务器收到操作Command: {}, 操作Key: {} ", command, key)
                if (message.children().size <= 1) {
                    ctx.writeAndFlush(ErrorRedisMessage("wrong number of arguments (given 0, expected 1)"))
                } else {
                    val dispatcherActor = ApplicationMain.managerActor
                    val redisCommand = RMessage(command, key, message, ctx)
                    dispatcherActor.tell(redisCommand)
                    //println("netty end time: ${System.currentTimeMillis()}")
                }
            }
        }
    }

    /**
     * 处理异常情况
     */
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        // cause.printStackTrace()
        // logger.warn("Client Close Ungracefully：{}", cause.message)
        ctx.close()
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        super.write(ctx, msg, promise)
    }

    /**
     * 首次建立连接时返回消息
     */
    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info("连接的客户端地址: {}", ctx.channel().remoteAddress())
        super.channelActive(ctx)
    }

    /**
     * 自定义实现逻辑（不使用）
     */
//    private fun printAggregatedRedisResponseRequest(message: ArrayRedisMessage): Any {
//        when ((message.children()[0] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)) {
//            "set" -> {
//                stringMap[(message.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)] =
//                        (message.children()[2] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)
//                return "ok"
//            }
//            "get" -> {
//                val res = stringMap[(message.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)]
//                return res ?: "(null)"
//            }
//            "del" -> {
//                stringMap.remove((message.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8))
//                return "ok"
//            }
//        }
//        return "(null)"
//    }

}