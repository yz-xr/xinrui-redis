package cn.yz.xr.common.entity

import cn.yz.xr.common.utils.MessageUtil
import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.RedisMessage
import io.netty.handler.codec.redis.SimpleStringRedisMessage
import java.lang.Exception
import java.util.*

class RNode(var score:Int, var member:String):Comparable<RNode>{
    override fun compareTo(other: RNode): Int {
        return this.score - other.score
    }
}

class RZSet(
        var zSet: LinkedHashMap<String,TreeSet<RNode>> = linkedMapOf(),
        var operationList: List<String> = listOf("ZADD")
) {
    private val comparator = kotlin.Comparator{ node1:RNode, node2:RNode->node2.score-node1.score}

    fun zAdd(array: List<String>):RedisMessage{
        var count = 0
        if(!zSet.containsKey(array[1])){
            this.zSet[array[1]] = sortedSetOf<RNode>(comparator)
        }
        for(i in 2 until array.size-1 step 2){
            this.zSet[array[1]]?.add(RNode(array[i].toInt(),array[i+1]))
            count+=1
        }
        return SimpleStringRedisMessage(count.toString())
    }

    fun zScore(array: List<String>):RedisMessage{
        // 参数数目校验
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 3)

        return if(paramError==null){
            if(this.zSet.containsKey(array[1])){
                try{
                    // 如果zSet中有
                    SimpleStringRedisMessage(zSet[array[1]]?.first { it.member == array[2] }?.score.toString())
                }catch (e:Exception){

                }
            }
            SimpleStringRedisMessage("(nil)")
        }else{
            paramError
        }
    }

    /**
     * 传入node和key，如果有相同member的node，则替换；没有node和key，则新增
     */
    private fun replace(key:String, node:RNode){
        if(this.zSet.containsKey(key)){
            val treeSet:TreeSet<RNode> = this.zSet[key]!!
            try{
                val pre = treeSet.first{it.member==node.member}
                treeSet.remove(pre)
                treeSet.add(node)
            }catch (e:Exception){
                treeSet.add(node)
            }
            this.zSet[key] = treeSet
        }else{
            this.zSet[key] = sortedSetOf<RNode>(comparator)
            this.zSet[key]?.add(node)
        }
    }

    fun zIncrBy(array: List<String>):RedisMessage{
        // 参数数目校验
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 4)

        return if(paramError==null){
            val intTransError = MessageUtil.checkString2Int(array[2])
            if(intTransError!=null){
                intTransError
            }else{
                // 该key是否已经存在
                if(this.zSet.containsKey(array[1])){
                    try{
                        // 有key和该node同名的node
                        val res:RNode = this.zSet[array[1]]?.first{ it->it.member==array[3]}!!
                        res.score = res.score + array[2].toInt()
                        replace(array[1], res)
                        SimpleStringRedisMessage("${res.score}")
                    }catch (e:Exception){
                        // 有key但没有与该node同名的node
                        this.zSet[array[1]]?.add(RNode(array[2].toInt(),array[3]))
                        SimpleStringRedisMessage("${array[2].toInt()}")
                    }
                }else{
                    this.zSet[array[1]] = sortedSetOf<RNode>(comparator)
                    this.zSet[array[1]]?.add(RNode(array[2].toInt(), array[3]))
                    SimpleStringRedisMessage("${array[2].toInt()}")
                }
            }
        }else{
            paramError
        }
    }

    fun operation(command: String, array: List<String>): RedisMessage {
        return when(command){
            else -> ErrorRedisMessage("not supported command")
        }
    }
}