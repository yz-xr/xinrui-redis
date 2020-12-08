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
import io.netty.buffer.ByteBufUtil
import io.netty.handler.codec.redis.FullBulkStringRedisMessage

class ManagerActorTest(
        context: ActorContext<Any>,
        private val max: Int,
        private var childArray: ArrayList<ActorRef<Command>> = arrayListOf(),
        private var response: String = ""
) : AbstractBehavior<Any>(context) {

    private val stringMap: MutableMap<Any, Any> = HashMap()

    init {
        println("children actor init")
//        for (i in 0..max){
//            this.childArray.add(i, context.spawn(ProcessActor.create(), "ProcessActor-${i}"))
//        }
    }

    companion object {
        fun create(max: Int): Behavior<Any> {
            return Behaviors.setup { context: ActorContext<Any> ->
                ManagerActorTest(context, max)
            }
        }
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder().onMessage<RMessage>(
                RMessage::class.java
        ) { message: RMessage -> this.onCommand(message) }
//            .onMessage<String>(
//                String::class.java
//            ){message: String -> this.returnRes(message) }
                .build()
    }

    // 接受command命令，并分配给子actor处理
    private fun onCommand(message: RMessage): Behavior<Any> {
        println("ManagerActorTest received message")
        val channel = message.channel
        when (message.command) {
            "set" -> {
                stringMap[(message.content.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)] =
                        (message.content.children()[2] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)
                 val fullBulkStringRedisMessage = FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(channel.alloc(), "ok"))
                 channel.writeAndFlush(fullBulkStringRedisMessage)
            }
            "get" -> {
                val res = stringMap[(message.content.children()[1] as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8)]
                val fullBulkStringRedisMessage = FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(channel.alloc(), res.toString() ?: "(null)"))
                channel.writeAndFlush(fullBulkStringRedisMessage)
            }
            else -> {
                channel.writeAndFlush("(null)")
            }
        }


//        val (time,content,clientId) = command
//        childArray[clientId % max].tell(command)
        return this
    }

    // 接受子actor的s值返回
//    private fun returnRes(response: String):Behavior<Any> {
//        this.response = response
//        println(response)
//        return this
//    }
//
//    fun getChildrenArray():ArrayList<ActorRef<Command>>{
//        return this.childArray
//    }

}