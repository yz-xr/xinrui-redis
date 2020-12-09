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
import io.netty.handler.codec.redis.ErrorRedisMessage

/**
 * 子actor根据命令，将之分配给不同的对象进行处理
 */
class ProcessActor(
        context: ActorContext<Any>,
        private var father: ActorRef<Any>,
        private var rString: RString = RString(),
        private var rList: RList = RList(),
        private var rHash: RHash = RHash(),
        private var rSet: RSet = RSet(),
        private var rZSet: RZSet = RZSet()
) : AbstractBehavior<Any>(context) {
    companion object {
        fun create(father: ActorRef<Any>): Behavior<Any> {
            return Behaviors.setup { context: ActorContext<Any> ->
                ProcessActor(context,father)
            }
        }
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
                .onMessage(
                        RMessage::class.java
                ) { command: RMessage -> onProcess(command)}
                .build()
    }

    private fun onProcess(message: RMessage): Behavior<Any> {
        val (command, content, channel, _) = message
        val arrays = MessageUtil.convertToArray(content)
        val type = command.toUpperCase()
        if(judgeRepetition(type,arrays)){
            // SimpleStringRedisMessage  单行输出文本
            // IntegerRedisMessage(1)    整数输出文本
            // ErrorRedisMessage()       错误输出文本
            // FullBulkStringRedisMessage    多行输出文本
            // ArrayRedisMessage             数组输出文本
            channel.write(ErrorRedisMessage("WRONGTYPE Operation against a key holding the wrong kind of value"))
        }else{
            val response = when (type) {
                in this.rString.operationList -> this.rString.operation(type, arrays)
                in this.rList.operationList -> this.rList.operation(type, arrays)
                in this.rHash.operationList -> this.rHash.operation(type, arrays)
                in this.rSet.operationList -> this.rSet.operation(type, arrays)
                in this.rZSet.operationList -> this.rZSet.operation(type, arrays)
                else -> {
                    // 不匹配
                    ErrorRedisMessage("I'm sorry, I don't recognize that command.")
                }
            }
            //val fullBulkStringRedisMessage = FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(channel.alloc(), response))
            channel.writeAndFlush(response)
        }
        return this
    }

    // 判断相同的key是否在系统中存在，但是类型不同。如果存在，则报错
    private fun judgeRepetition(type:String, arrys:List<String>):Boolean{
        if(type in listOf<String>("SET","HSET","LSET","SADD","ZADD")){
            when(type){
                "SET" -> return arrys[1] in rList.listMap.keys.union(rHash.hash.keys).union(rSet.rset.keys)
                "HSET" -> return arrys[1] in rList.listMap.keys.union(rString.map.keys).union(rSet.rset.keys)
                "LSET" -> return arrys[1] in rHash.hash.keys.union(rString.map.keys).union(rSet.rset.keys)
                "SADD" -> return arrys[1] in rList.listMap.keys.union(rString.map.keys).union(rHash.hash.keys)
                "ZADD" -> return arrys[1] in rList.listMap.keys.union(rString.map.keys).union(rHash.hash.keys).union(rSet.rset.keys)
            }
        }
        return false
    }
}
