package cn.yz.xr.common.entity

import cn.yz.xr.common.utils.MessageUtil
import io.netty.handler.codec.redis.*
import java.lang.Exception
import java.util.*

/**
 * author：雷克萨
 */
class RNode(var score:Int, var member:String):Comparable<RNode>{
    override fun compareTo(other: RNode): Int {
        return this.score - other.score
    }
}

class RZSet(
        var zSet: LinkedHashMap<String,TreeSet<RNode>> = linkedMapOf(),
        var operationList: List<String> = listOf("ZADD","ZSCORE","ZINCRBY","ZCARD","ZCOUNT","ZRANGE","ZRANK","ZREM")
) {
    private val comparator = kotlin.Comparator{ node1:RNode, node2:RNode->node2.score-node1.score}

    fun ZAdd(array: List<String>):RedisMessage{
        if(array.size < 4){
            return ErrorRedisMessage("wrong number of arguments (given ${array.size}, expected 4)")
        }
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

    fun ZScore(array: List<String>):RedisMessage {
        // 参数数目校验
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 3)

        if(paramError==null){
            if(this.zSet.containsKey(array[1])){
                for(o in zSet[array[1]]!!){
                    if(o.member==array[2]){
                        return SimpleStringRedisMessage(o.score.toString())
                    }
                }
            }
        }else{
            return paramError
        }
        return SimpleStringRedisMessage("(nil)")
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

    fun ZIncrBy(array: List<String>):RedisMessage{
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

    fun ZCard(array: List<String>):RedisMessage{

        // 参数数目校验
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 2)

        return if(paramError==null){
            if(this.zSet.containsKey(array[1])){
                val length:Long = this.zSet[array[1]]?.size?.toLong()!!
                IntegerRedisMessage(length)
            }else{
                IntegerRedisMessage(0L)
            }
        }else{
            paramError
        }
    }

    fun ZCount(array: List<String>):RedisMessage{
        // 参数数目校验
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 4)

        return if(paramError==null){
            if(this.zSet.containsKey(array[1])){
                val min = array[2].toInt()
                val max = array[3].toInt()
                var count = 0L
                this.zSet[array[1]]?.forEach { it-> if (it.score < max + 1 && it.score > min - 1){count+=1} }
                IntegerRedisMessage(count)
            }else{
                IntegerRedisMessage(0L)
            }
        }else{
            paramError
        }
    }

    fun ZRange(array: List<String>):RedisMessage{
        return if(array.size in listOf(4,5)){
            var start = array[2].toInt()
            var end = array[3].toInt()
            val hasScore = array.size==5
            if(this.zSet.containsKey(array[1])){
                if(start < 0){
                    start += this.zSet[array[1]]?.size ?: 0
                }
                if(end < 0){
                    end += this.zSet[array[1]]?.size ?: 0
                }
                if(start > end || start > this.zSet[array[1]]?.size ?:0 ){
                    SimpleStringRedisMessage("(empty list or set)")
                }else{
                    if(start < 0){
                        SimpleStringRedisMessage("(empty list or set)")
                    }
                    if(end < 0){
                        SimpleStringRedisMessage("(empty list or set)")
                    }
                    var index = 0
                    var lineIndex = 1
                    val varList = mutableListOf<RedisMessage>()
                    for(i in this.zSet[array[1]]!!){
                        if(index in start..end){
                            varList.add(SimpleStringRedisMessage(i.member))
                            lineIndex+=1
                            if(hasScore){
                                varList.add(SimpleStringRedisMessage(i.score.toString()))
                                lineIndex+=1
                            }
                        }
                        index+=1
                    }
                    ArrayRedisMessage(varList)
                }
            }else{
                SimpleStringRedisMessage("(empty list or set)")
            }
        }else{
            ErrorRedisMessage("wrong number of arguments (given ${array.size}, expected 3)")
        }
    }

    fun ZRank(array: List<String>):RedisMessage{
        // 参数数目校验
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 3)

        if(paramError==null){
            if(this.zSet.containsKey(array[1]) && this.zSet[array[1]]?.size ?: -1 > 0){
                var count = 0
                for(node in this.zSet[array[1]]!!){
                    if(node.member==array[2]){
                        return IntegerRedisMessage(count.toLong())
                    }
                    count+=1
                }
            }
            return SimpleStringRedisMessage("(nil)")
        }else{
            return paramError
        }
    }

    fun ZREM(array: List<String>):RedisMessage{
        return if(array.size > 2){
            var count = 0L
            for(i in 2 until array.size){
                if(this.zSet.remove(array[i])!=null){
                    count+=1
                }
            }
            IntegerRedisMessage(count)
        }else{
           ErrorRedisMessage("ERR wrong number of arguments for 'zrem' command")
        }
    }

    fun operation(command: String, array: List<String>): RedisMessage {
        return when(command){
            "ZADD"->ZAdd(array)
            "ZSCORE"->ZScore(array)
            "ZINCRBY"->ZIncrBy(array)
            "ZCARD"->ZCard(array)
            "ZCOUNT"->ZCount(array)
            "ZRANGE"->ZRange(array)
            "ZRANK"->ZRank(array)
            "ZREM"->ZREM(array)
            else -> ErrorRedisMessage("not supported command")
        }
    }
}