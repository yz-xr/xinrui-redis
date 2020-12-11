package cn.yz.xr

import akka.actor.typed.ActorSystem
import cn.yz.xr.consumer.server.RedisServerApp
import cn.yz.xr.producer.ManagerActor
import cn.yz.xr.producer.ManagerActorSingle
import cn.yz.xr.producer.ManagerRouterActor
import java.util.*

/**
 * yzxr-redis 主启动类
 * @author lewy
 */
class ApplicationMain {

    fun run() {
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

        // 测试1 ： 单 actor 实例，无并行子 actor 的情况
        // val managerActor: ActorSystem<Any> = ActorSystem.create(ManagerActorSingle.create(8), "ManagerActorSingle")
        // 测试2 ： 父子actor，8个子actor并行，未引入Router，由父actor根据key取模实现分发
        // val managerActor: ActorSystem<Any> = ActorSystem.create(ManagerActor.create(8), "ManagerActor")
        // 测试3 ： 父子actor，8个子actor并行，引入Router，由router根据key取模（一致性hash协议）进行分发
        val managerActor: ActorSystem<Any> = ActorSystem.create(ManagerRouterActor.create(8), "ManagerRouterActor")
        // 测试4： 8个并行actor，直接在netty端完成根据取模找到对应的actor完成分发
        // 待实现

    }
}

fun main(args: Array<String>) {
    ApplicationMain().run()
}



