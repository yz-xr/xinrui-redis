package cn.yz.xr.producer

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.*
import cn.yz.xr.common.entity.RCommon
import cn.yz.xr.common.entity.repo.RMessage
import cn.yz.xr.common.utils.StrategyUtil
import cn.yz.xr.producer.communication.CommonData


/**
 * author:雷克萨
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
                .onMessage<CommonData>(
                        CommonData::class.java
                ){res:CommonData->this.onOtherCommand(res)}
                .build()
    }

    // 接受command命令，使用相应的策略分配给对应的子actor，并分配给子actor处理
    private fun onCommand(message: RMessage): Behavior<Any> {
        val (command, key, _, _, _) = message
        // 一些需要借助其他actor的命令，在此处定义
        if(command in rCommon.operationList){
            for(child in childArray){
                child.tell(CommonData(message, null))
                context.watchWith(child, CommonData(message, null))
            }
        }else{
            StrategyUtil.scheduleActor(key,childArray).tell(message)
        }
        return this
    }

    private fun onOtherCommand(res:CommonData):Behavior<Any>{
        return this
    }
}

