package cn.yz.xr.producer

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import cn.hutool.core.util.CharsetUtil
import cn.yz.xr.common.Command
import cn.yz.xr.common.RMessage
import io.netty.handler.codec.redis.FullBulkStringRedisMessage

class ManagerActor(
        context: ActorContext<Any>,
        private val max: Int,
        private var childArray: ArrayList<ActorRef<RMessage>> = arrayListOf(),
        private var response: String = ""
) : AbstractBehavior<Any>(context){

    init {
        for (i in 0..max){
            this.childArray.add(i, context.spawn(ProcessActor.create(), "ProcessActor-${i}"))
        }
    }

    companion object{
        fun create(max:Int): Behavior<Any> {
            return Behaviors.setup{
                    context: ActorContext<Any> ->
                ManagerActor(context,max)
            }
        }
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
            .onMessage<String>(
                String::class.java
            ){message: String -> this.returnRes(message) }
                .onMessage<RMessage>(
                        RMessage::class.java
                ) { message: RMessage -> this.onCommand(message) }
            .build()
    }

    // 接受command命令，并分配给子actor处理
    private fun onCommand(message: RMessage):Behavior<Any>{
        val (_,content, _, _) = message
        val key = (content.children()[0] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)
        childArray[key.hashCode() % max].tell(message)
        return this
    }

    // 接受子actor的s值返回
    private fun returnRes(response: String):Behavior<Any> {
        this.response = response
        println(response)
        return this
    }

    fun getChildrenArray():ArrayList<ActorRef<RMessage>>{
        return this.childArray
    }

}