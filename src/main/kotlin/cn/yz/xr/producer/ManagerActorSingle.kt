package cn.yz.xr.producer

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import cn.yz.xr.common.entity.*
import cn.yz.xr.common.entity.repo.RMessage
import cn.yz.xr.common.utils.MessageUtil
import io.netty.handler.codec.redis.ErrorRedisMessage


/**
 * 单 actor 实例，无并行子 actor 的情况
 */
class ManagerActorSingle(
        context: ActorContext<Any>,
        max: Int,
        private var rString: RString = RString(),
        private var rList: RList = RList(),
        private var rHash: RHash = RHash(),
        private var rSet: RSet = RSet(),
        private var rZSet: RZSet = RZSet()
) : AbstractBehavior<Any>(context) {

    companion object {

        fun create(max: Int): Behavior<Any> {
            return Behaviors.setup { context ->
                ManagerActorSingle(context, max)
            }
        }
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
                .onMessage<RMessage>(RMessage::class.java) { message: RMessage -> this.onCommand(message) }.build()
    }

    // 接受command命令，使用相应的策略分配给对应的子actor，并分配给子actor处理
    private fun onCommand(message: RMessage): Behavior<Any> {
        val (command, key, content, channel, _) = message
        val arrays = MessageUtil.convertToArray(content)
        val type = command.toUpperCase()
        if (judgeRepetition(type, arrays)) {
            channel.writeAndFlush(ErrorRedisMessage("WRONGTYPE Operation against a key holding the wrong kind of value"))
        } else {
            val response = when (type) {
                in this.rString.operationList -> this.rString.operation(type, key, arrays)
                in this.rList.operationList -> { this.rList.operation(type, arrays) }
                in this.rHash.operationList -> this.rHash.operation(type, arrays)
                in this.rSet.operationList -> this.rSet.operation(type, arrays)
                in this.rZSet.operationList -> this.rZSet.operation(type, arrays)
                else -> {
                    ErrorRedisMessage("I'm sorry, I don't recognize that command.") // 不匹配
                }
            }
            channel.writeAndFlush(response)
        }
        return this
    }

    // 判断相同的key是否在系统中存在，但是类型不同。如果存在，则报错
    private fun judgeRepetition(type: String, arrys: List<String>): Boolean {
        if (type in listOf("SET", "HSET", "LPUSH", "SADD", "ZADD")) {
            when (type) {
                "SET" -> return arrys[1] in rList.listMap.keys.union(rHash.listMap.keys).union(rSet.rset.keys)
                "HSET" -> return arrys[1] in rList.listMap.keys.union(rString.map.keys).union(rSet.rset.keys)
                "LPUSH" -> return arrys[1] in rHash.listMap.keys.union(rString.map.keys).union(rSet.rset.keys)
                "SADD" -> return arrys[1] in rList.listMap.keys.union(rString.map.keys).union(rHash.listMap.keys)
                "ZADD" -> return arrys[1] in rList.listMap.keys.union(rString.map.keys).union(rHash.listMap.keys).union(rSet.rset.keys)
            }
        }
        return false
    }
}