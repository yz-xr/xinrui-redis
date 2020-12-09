package cn.yz.xr.common.entity

import cn.yz.xr.common.utils.SkipList
import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.RedisMessage
import io.netty.handler.codec.redis.SimpleStringRedisMessage
import java.lang.Exception

class RZSet(
        var zSet: MutableMap<String,SkipList> = mutableMapOf<String,SkipList>(),
        var operationList: List<String> = listOf("ZADD")
) {
    private fun zAdd(array: List<String>):RedisMessage{
        try{
            if(this.zSet[array[0]] is SkipList){
                for(i in 1..array.size step 2){
                    this.zSet[array[0]]?.insert(array[i],array[i+1].toInt())
                }
            }else{
                val skipList = SkipList()
                for(i in 1..array.size step 2){
                    skipList.insert(array[i],array[i+1].toInt())
                }
                this.zSet[array[0]] = skipList
            }
        }catch (e:Exception){
            return ErrorRedisMessage("ERR syntax error")
        }
        return SimpleStringRedisMessage("")
    }

    fun operation(command: String, array: List<String>): RedisMessage {
        return when(command){
            "ZADD" -> zAdd(array)
            else -> ErrorRedisMessage("not supported command")
        }
    }
}

fun main(args:Array<String>){
    val skiplist = SkipList()
    skiplist.insert("kuang",1)
    skiplist.insert("lala",5)
    skiplist.insert("lala",4)
    skiplist.deleteByScore(4)
    println(skiplist)
}