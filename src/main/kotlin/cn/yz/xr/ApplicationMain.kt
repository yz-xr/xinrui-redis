package cn.yz.xr

import akka.actor.typed.ActorSystem
import cn.yz.xr.consumer.server.RedisServerApp
import cn.yz.xr.producer.ManagerActor
import java.util.*

/**
 * yzxr-redis 主启动类
 */
class ApplicationMain {

    fun run(): Unit {
        val resourceBundle: ResourceBundle = ResourceBundle.getBundle("config")
        val portStr: String = resourceBundle.getString("redis.port")
        if (portStr.isNotEmpty()) {
            try {
                port = portStr.toInt()
            } catch (e: Exception) {
                throw IllegalArgumentException("端口配置不正确")
            }
        } else {
            throw RuntimeException("找不到端口号，请配置")
        }

        /**
         * Netty 服务器启动
         */
        val rsa = RedisServerApp()
        rsa.run()
    }

    /**
     * 初始化：
     * 1. 加载配置文件；
     * 2. 启动 ActorSystem；
     */
    companion object ApplicationInit {
        // 读取配置文件，获取端口信息
        var port = 0
        // Akka 启动
        val managerActor: ActorSystem<Any> = ActorSystem.create(ManagerActor.create(10), "ManagerActor")
    }
}

fun main(args: Array<String>) {
    ApplicationMain().run()
}



