package cn.yz.xr

import cn.yz.xr.consumer.server.RedisServerApp

/**
 * yzxr-redis 主启动类
 */
fun main(args: Array<String>) {


    /**
     * Akka 启动
     */
    //val managerActor: ActorSystem<Any> = ActorSystem.create(ManagerActor.create(10), "ManagerActor")

    /**
     * Netty 服务器启动
     */
    val rsa: RedisServerApp = RedisServerApp()
    rsa.run()

}