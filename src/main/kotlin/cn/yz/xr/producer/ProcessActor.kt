package cn.yz.xr.producer

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import cn.yz.xr.common.*
import io.netty.buffer.ByteBufUtil
import io.netty.handler.codec.redis.FullBulkStringRedisMessage

class ProcessActor(
    context: ActorContext<RMessage>,
    private var rString:Rstring = Rstring(),
    private var rList: Rlist = Rlist(),
    private var rHash: Rhash = Rhash(),
    private var rSet: Rset = Rset(),
    private var rZSet: ZSet = ZSet("hello")
) : AbstractBehavior<RMessage> (context){

    companion object{
        fun create(): Behavior<RMessage> {
            return Behaviors.setup{
                    context: ActorContext<RMessage> ->
                ProcessActor(context)
            }
        }
    }

    override fun createReceive(): Receive<RMessage> {
        return newReceiveBuilder()
            .onMessage(
                RMessage::class.java
            ) { command:  RMessage ->
                onProcess(command)
            }.build()
    }

    private fun onProcess(message: RMessage): Behavior<RMessage>{
        val (command, content, channel, timestamp) = message
        val arrays = Util.convertToArray(content)
        val response = when(command.toUpperCase()){
            in this.rString.operationList -> this.rString.operation(command.toUpperCase(), arrays)
            in this.rList.operationList -> this.rList.operation(command, arrays)
            in this.rHash.operationList -> this.rHash.operation(command, arrays)
            in this.rSet.operationList -> this.rSet.operation(command, arrays)
            in this.rZSet.operationList -> this.rZSet.operation(command, arrays)
            else -> otherProcess(arrays)
        }
        val fullBulkStringRedisMessage = FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(channel.alloc(), response))
        channel.writeAndFlush(fullBulkStringRedisMessage)
        return this
    }

    private fun otherProcess(array: List<String>) : String{
        return "not support command"
    }
}