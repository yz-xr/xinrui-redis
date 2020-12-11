package cn.yz.xr.consumer.server

import cn.yz.xr.ApplicationMain
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.redis.RedisArrayAggregator
import io.netty.handler.codec.redis.RedisBulkStringAggregator
import io.netty.handler.codec.redis.RedisDecoder
import io.netty.handler.codec.redis.RedisEncoder
import kotlin.jvm.Throws
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author abc
 */
class RedisServerApp {

    private val logger: Logger = LoggerFactory.getLogger(RedisServerApp::class.java)

    fun run() {
        val serverBootstrap = ServerBootstrap()
        val group: EventLoopGroup = NioEventLoopGroup()
        try {
            serverBootstrap.group(group)
            serverBootstrap.childHandler(object : ChannelInitializer<SocketChannel>() {
                @Throws(Exception::class)
                override fun initChannel(socketChannel: SocketChannel) {
                    socketChannel.pipeline()
                            .addLast(RedisDecoder())
                            .addLast(RedisBulkStringAggregator())
                            .addLast(RedisArrayAggregator())
                            .addLast(RedisEncoder())
                            .addLast(RedisServerHandler())
                }
            })

            serverBootstrap.channel(NioServerSocketChannel::class.java)
            val channelFuture = serverBootstrap.bind(ApplicationMain.port).sync()
            logger.info("服务器启动成功！")
            channelFuture.channel().closeFuture().sync()
        } finally {
            group.shutdownGracefully()
        }
    }
}