package cn.yz.xr.producer

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.javadsl.*
import cn.yz.xr.common.entity.RCommon
import cn.yz.xr.common.entity.repo.RMessage
import cn.yz.xr.producer.communication.CommonData
import java.util.function.Function

/**
 * 父子actor，8个并行子actor，使用Router
 */
class ManagerRouterActor(
        context: ActorContext<Any>,
        max: Int,
        private var rCommon: RCommon = RCommon()
) : AbstractBehavior<Any>(context) {

    companion object {

        private lateinit var processors: ActorRef<Any>

        fun create(max: Int): Behavior<Any> {
            return Behaviors.setup { context ->
                val processAB = Routers.pool(max,
                        Behaviors.supervise(ProcessRouterActor.create(context.self))
                                .onFailure(SupervisorStrategy.restart()))
                        // 两个参数：
                        // 参数一：virtualNodesFactor, 实际节点数 = virtualNodesFactor * routee num
                        // 参数二：function, 表示 key mapping 策略
                        .withConsistentHashingRouting(2, Function {
                            (it as RMessage).Key
                        })
                // 创建 processActor
                processors = context.spawn(processAB, "processActor-route")
                ManagerRouterActor(context, max)
            }
        }
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
                .onMessage<RMessage>(
                        RMessage::class.java
                ) { message: RMessage ->
                    this.onCommand(message)
                }
                .onMessage<CommonData>(
                        CommonData::class.java
                ) { res: CommonData -> this.onOtherCommand(res) }
                .build()
    }

    // 接受command命令，使用相应的策略分配给对应的子actor，并分配给子actor处理
    private fun onCommand(message: RMessage): Behavior<Any> {
        val (command, key, content, _, _) = message
        processors.tell(message)
        return this
    }

    private fun onOtherCommand(res: CommonData):Behavior<Any>{
        val (command,data) = res
        return this
    }
}