package cn.yz.xr.common.entity

import akka.actor.typed.ActorRef
import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.RedisMessage
import io.netty.handler.codec.redis.SimpleStringRedisMessage

class RCommon(
        var brothers: ArrayList<ActorRef<Any>> = arrayListOf(),
        var operationList:List<String> = listOf("KEYS","MSET","MGET","SETNX")
){
    public fun setBothers(brothers: ArrayList<ActorRef<Any>>){
        this.brothers = brothers
    }

    private fun keys(): RedisMessage{
//        for(brother in brothers){
//            brother.tell()
//        }
        return SimpleStringRedisMessage("")
    }

    private fun mset():RedisMessage{
        for(i in 0..9){
            println(brothers[i])
        }
        return SimpleStringRedisMessage("")
    }

    fun operation(command: String, array: List<String>): RedisMessage {
        return when(command){
            "KEYS" -> keys()
            "MSET" -> mset()
            else -> ErrorRedisMessage("not supported command")
        }
    }
}