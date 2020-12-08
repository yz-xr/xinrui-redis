package cn.yz.xr.producer

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import cn.yz.xr.common.entity.*
import cn.yz.xr.common.entity.repo.RMessage
import cn.yz.xr.common.utils.MessageUtil
import io.netty.buffer.ByteBufUtil
import io.netty.handler.codec.redis.FullBulkStringRedisMessage

class ProcessActor(
        context: ActorContext<RMessage>,
        private var rString: RString = RString(),
        private var rList: RList = RList(),
        private var rHash: RHash = RHash(),
        private var rSet: RSet = RSet(),
        private var rZSet: RZset = RZset("hello")
) : AbstractBehavior<RMessage>(context) {

    companion object {
        fun create(): Behavior<RMessage> {
            return Behaviors.setup { context: ActorContext<RMessage> ->
                ProcessActor(context)
            }
        }
    }

    override fun createReceive(): Receive<RMessage> {
        return newReceiveBuilder()
                .onMessage(
                        RMessage::class.java
                ) { command: RMessage ->
                    onProcess(command)
                }.build()
    }

    private fun onProcess(message: RMessage): Behavior<RMessage> {
        val (command, content, channel, timestamp) = message
        val arrays = MessageUtil.convertToArray(content)
        var type = command.toUpperCase()
        val response = when (type) {
            in this.rString.operationList -> this.rString.operation(type, arrays)
            in this.rList.operationList -> this.rList.operation(type, arrays)
            in this.rHash.operationList -> this.rHash.operation(type, arrays)
            in this.rSet.operationList -> this.rSet.operation(type, arrays)
            in this.rZSet.operationList -> this.rZSet.operation(type, arrays)
            else -> otherProcess(arrays)
        }
        val fullBulkStringRedisMessage = FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(channel.alloc(), response))
        channel.writeAndFlush(fullBulkStringRedisMessage)
        return this
    }

    private fun otherProcess(array: List<String>): String {
        return "not support command"
    }
}