package cn.yz.xr.producer

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import cn.hutool.core.util.CharsetUtil
import cn.yz.xr.common.entity.RCommon
import cn.yz.xr.common.entity.repo.RMessage
import cn.yz.xr.common.utils.StrategyUtil
import io.netty.handler.codec.redis.FullBulkStringRedisMessage

/**
 * 父actor负责接受Netty的命令，并将命令分配给子actor执行
 */
class ManagerActor(
        context: ActorContext<Any>,
        max: Int,
        private var childArray: ArrayList<ActorRef<Any>> = arrayListOf(),
        private var rCommon: RCommon = RCommon()
) : AbstractBehavior<Any>(context) {

    init {
        for (i in 0 until max) {
            this.childArray.add(i, context.spawn(ProcessActor.create(context.self), "ProcessActor-${i}"))
        }
    }

    companion object {
        fun create(max: Int): Behavior<Any> {
            return Behaviors.setup { context: ActorContext<Any> ->
                ManagerActor(context, max)
            }
        }
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
                .onMessage<RMessage>(
                        RMessage::class.java
                ) { message: RMessage -> this.onCommand(message) }
                .build()
    }

    // 接受command命令，使用相应的策略分配给对应的子actor，并分配给子actor处理
    private fun onCommand(message: RMessage): Behavior<Any> {
        val (command, content, _, _) = message
        if(command in rCommon.operationList){
            otherProcess(message)
        }else{
            val key = (content.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)
            StrategyUtil.scheduleActor(key,childArray).tell(message)
        }
        return this
    }

    private fun otherProcess(message: RMessage):Behavior<Any>{
        return this
    }
}

