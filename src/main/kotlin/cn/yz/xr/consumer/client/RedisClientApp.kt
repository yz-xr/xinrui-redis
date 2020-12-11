package cn.yz.xr.consumer.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.redis.RedisArrayAggregator
import io.netty.handler.codec.redis.RedisBulkStringAggregator
import io.netty.handler.codec.redis.RedisDecoder
import io.netty.handler.codec.redis.RedisEncoder
import io.netty.util.concurrent.GenericFutureListener
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.jvm.Throws

/**
 * @author abc
 */
class RedisClientApp {

    fun run(): Unit {
        val group = NioEventLoopGroup()
        try {
            val bootstrap = Bootstrap()
            bootstrap.group(group).channel(NioSocketChannel::class.java)
                    .handler(object : ChannelInitializer<SocketChannel>() {
                        @Throws(Exception::class)
                        override fun initChannel(socketChannel: SocketChannel) {
                            socketChannel.pipeline()
                                    .addLast(RedisDecoder())
                                    .addLast(RedisBulkStringAggregator())
                                    .addLast(RedisArrayAggregator())
                                    .addLast(RedisEncoder())
                                    .addLast(RedisClientHandler())
                        }
                    })
            val channel = bootstrap.connect(host, port).sync().channel()
            println("Enter Redis commands (quit to end)")
            var channelFuture: ChannelFuture? = null
            val readIn = BufferedReader(InputStreamReader(System.`in`))
            while (true) {
                val input: String = readIn.readLine()
                val line: String = input.trim()
                if ("quit".equals(line, true)) {
                    channel.close().sync()
                    break
                } else if (line.isEmpty()) {
                    continue
                }
                channelFuture = channel.writeAndFlush(line)
                channelFuture.addListener {
                    GenericFutureListener<ChannelFuture> {
                        if (!it.isSuccess) {
                            println("write failed: ${it.cause().printStackTrace(System.err)}")
                        }
                    }
                }
            }
            channelFuture?.sync()

        } finally {
            group.shutdownGracefully()
        }
    }

    companion object {
        val host: String = System.getProperty("host", "127.0.0.1")
        val port = System.getProperty("port", "6379").toInt()
    }
}

fun main(args: Array<String>) {
    val rca = RedisClientApp()
    rca.run()
}