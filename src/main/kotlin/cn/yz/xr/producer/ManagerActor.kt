package cn.yz.xr.producer

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import cn.hutool.core.util.CharsetUtil
import cn.yz.xr.common.entity.repo.RMessage
import cn.yz.xr.common.utils.StrategyUtil
import cn.yz.xr.producer.communication.PostBrother
import cn.yz.xr.producer.communication.QueryBrother
import io.netty.handler.codec.redis.FullBulkStringRedisMessage

/**
 * 父actor负责接受Netty的命令，并将命令分配给子actor执行
 */
class ManagerActor(
        context: ActorContext<Any>,
        max: Int,
        private var childArray: ArrayList<ActorRef<Any>> = arrayListOf()
) : AbstractBehavior<Any>(context) {

    init {
        for (i in 0 until max) {
            this.childArray.add(i, context.spawn(ProcessActor.create(context.self), "ProcessActor-${i}"))
        }
        // 初始化RCommon的brothers屬性，即所有子actor的列表
        for (i in 0 until max){
            context.self.tell(QueryBrother(this.childArray[i]))
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
                .onMessage<QueryBrother>(
                        QueryBrother::class.java
                ){message:QueryBrother -> this.getChildList(message)}
                .build()
    }

    // 接受command命令，使用相应的策略分配给对应的子actor，并分配给子actor处理
    private fun onCommand(message: RMessage): Behavior<Any> {
        val (_, content, _, _) = message
        val key = (content.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)
        StrategyUtil.scheduleActor(key,childArray).tell(message)
        return this
    }

    private fun getChildList(message:QueryBrother):Behavior<Any>{
        message.replyTo.tell(PostBrother(childArray))
        return this
    }
}

