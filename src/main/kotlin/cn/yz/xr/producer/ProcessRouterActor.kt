package cn.yz.xr.producer

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import cn.yz.xr.common.entity.*
import cn.yz.xr.common.entity.repo.RMessage
import cn.yz.xr.common.utils.MessageUtil
import cn.yz.xr.consumer.server.RedisServerHandler
import cn.yz.xr.producer.communication.CommonData
import io.netty.handler.codec.redis.ErrorRedisMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 子actor根据命令，将之分配给不同的对象进行处理
 * @author lewy
 */
class ProcessRouterActor(
        context: ActorContext<Any>,
        private var father: ActorRef<Any>,
        private var rString: RString = RString(),
        private var rList: RList = RList(),
        private var rHash: RHash = RHash(),
        private var rSet: RSet = RSet(),
        private var rZSet: RZSet = RZSet()
) : AbstractBehavior<Any>(context) {

    private val logger: Logger = LoggerFactory.getLogger(RedisServerHandler::class.java)

    companion object {
        fun create(father: ActorRef<Any>): Behavior<Any> {
            return Behaviors.setup { context: ActorContext<Any> ->
                ProcessRouterActor(context, father)
            }
        }
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
                .onMessage(
                        RMessage::class.java
                ) { command: RMessage -> onProcess(command) }
                .onMessage(
                        CommonData::class.java
                ) { commonData: CommonData -> otherProcess(commonData) }
                .build()
    }

    private fun otherProcess(commonData: CommonData): Behavior<Any> {

        val (rMessage, _) = commonData
        val (command, _, _, _) = rMessage
        val res = when (command) {
            "KEYS" -> rList.listMap.keys.union(rHash.listMap.keys).union(rSet.rset.keys)
            else -> ""
        }
        father.tell(CommonData(rMessage, res))
        return this
    }

    private fun onProcess(message: RMessage): Behavior<Any> {
        // logger.info("children actor: {}", context.toString())

        val (command, key, content, channel, _) = message
        val arrays = MessageUtil.convertToArray(content)
        val type = command.toUpperCase()
        //println("ProcessActorRouterTest arrays: $arrays")
        //println("ProcessActorRouterTest type: $type")
        if (judgeRepetition(type, arrays)) {
            channel.writeAndFlush(ErrorRedisMessage("WRONGTYPE Operation against a key holding the wrong kind of value"))
        } else {
            val response = when (type) {
                in this.rString.operationList -> {
                    //println("akka end time: ${System.currentTimeMillis()}")
                    this.rString.operation(type, key, arrays)
                }
                in this.rList.operationList -> {
                    //println(rList.listMap)
                    this.rList.operation(type, arrays)
                }
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
