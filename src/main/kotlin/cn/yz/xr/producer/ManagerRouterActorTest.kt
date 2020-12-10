package cn.yz.xr.producer

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.javadsl.*
import cn.yz.xr.common.entity.RCommon
import cn.yz.xr.common.entity.repo.RMessage
import cn.yz.xr.common.utils.MessageUtil
import io.netty.handler.codec.redis.RedisMessage
import java.util.function.Function


/**
 * 父actor负责接受Netty的命令，并将命令分配给子actor执行
 */
class ManagerRouterActorTest(
        context: ActorContext<Any>,
        max: Int,
        var router: ActorRef<Any>?,
        //private var childArray: ArrayList<ActorRef<Any>> = arrayListOf(),
        private var rCommon: RCommon = RCommon()
) : AbstractBehavior<Any>(context) {

    init {
        val pool = Routers.pool(max,
                Behaviors.supervise(ProcessActorRouterTest.create(context.self)).onFailure(SupervisorStrategy.restart()))
                // 两个参数：
                // 参数一：virtualNodesFactor, 实际节点数 = virtualNodesFactor * routee num
                // 参数二：function, 表示 key mapping 策略
                .withConsistentHashingRouting(1, Function {
                        val array = MessageUtil.convertToArray((it as RMessage).content)
                        //println(array[1])
                        array[1]
                })
                this.router = context.spawn(pool, "processActor-route")

//        for (i in 0..20) {
//            router.tell("msg: $i")
//        }

        //val blocking = pool


        //for (i in 0 until max) {
        //    // 产生10个子actor
        //    this.childArray.add(i, context.spawn(ProcessActor.create(context.self), "ProcessActor-${i}"))
        //}
    }

    companion object {
        fun create(max: Int): Behavior<Any> {
            return Behaviors.setup { context: ActorContext<Any> ->
                ManagerRouterActorTest(context, max, null)
            }
        }
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
                .onMessage<RMessage>(
                        RMessage::class.java
                ) { message: RMessage -> this.onCommand(message) }
//                .onMessage<CommonData>(
//                        CommonData::class.java
//                ) { res: CommonData -> this.onOtherCommand(res) }
                .onMessage(String::class.java) { res: String ->
                    this.testCommand(res)
                }
                .build()
    }

    private fun testCommand(res: String): Behavior<Any> {
        println("After Handler: " + res.toUpperCase())
        return this
    }

    // 接受command命令，使用相应的策略分配给对应的子actor，并分配给子actor处理
    private fun onCommand(message: RMessage): Behavior<Any> {
        val (command, content, _, _) = message
        // println("ManagerRouterActorTest onCommand: $command")
        this.router!!.tell(message)

        // 一些需要借助其他actor的命令，在此处定义
//        if (command in rCommon.operationList) {
//            for (child in childArray) {
//                child.tell(CommonData(message, null))
//                context.watchWith(child, CommonData(message, null))
//            }
//        } else {
//            val key = (content.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)
//            StrategyUtil.scheduleActor(key, childArray).tell(message)
//        }
        return this
    }

//    private fun onOtherCommand(res: CommonData): Behavior<Any> {
//        val (command, data) = res
//        return this
//    }
}

//fun main(args: Array<String>) {
//    val managerActor: ActorSystem<Any> = ActorSystem.create(ManagerRouterActorTest.create(8), "ManagerRouterActorTest")
//    managerActor.tell("hello yy")
//}

