package cn.yz.xr.common.entity

import io.netty.handler.codec.redis.*
import kotlin.math.absoluteValue
import kotlin.collections.MutableSet as MutableSet1
import kotlin.collections.setOf as setOf1

/**
 * @author adc
 */
class RSet(
        var rset: HashMap<String, MutableSet1<String>> = hashMapOf(),
        var operationList: List<String> = listOf("SADD",
                "SCARD","SDIFF","SDIFFSTORE","SINTER","SINTERSTORE"
                ,"SISMEMBER","SMEMBERS","SMOVE","SPOP","SRANDMEMBER"
                , "SREM","SUNION","SUNIONSTORE","SSCAN")
){

    //向集合添加一个或多个成员。。a加法√√√√√√√√√√√√√√√√√√√√√√√√√
    fun sadd(array: List<String>):RedisMessage {
        val key = array[1]
        val array = array-array[0]-array[1]
        if (rset.containsKey(key)) {
            var a = rset[key]!!.size
            for ( i in 0..array.size-1){
            if(!rset[key]!!.contains(array[i])){//判断value里面有没有值
                rset[key]?.add(array[i])
            }
        }
            a = rset[key]!!.size - a
            return  IntegerRedisMessage(a.toLong())
        }
        else{
            rset.put(key, mutableSetOf()) //创建哈希表并且每个value都装进去
            for ( i in 0..array.size-1){
                if(!rset[key]!!.contains(array[i])){//判断value里面有没有值
                    rset[key]?.add(array[i])
                }
            }
            return IntegerRedisMessage(rset[key]!!.size.toLong())
        }
    }

    //向集合添加一个或多个成员。。用数组
//    fun sadd(key:String,array: List<String>):IntegerRedisMessage {
//        var a = mutableSetOf<String>()
//        if (rset.containsKey(key)) {array.forEach {
//            //查找有没有key值
//            if(!rset[key]!!.contains(it))//判断value里面有没有值
//                a.add(it)
//            rset[key]!!.add(it)
//        }
//            return  IntegerRedisMessage(a.size.toLong())
//        }
//        else{array.forEach{
//            rset.put(key, mutableSetOf(it)) //创建哈希表并且每个value都装进去
//            a.add(it)
//            rset[key]!!.add(it)
//        }
//            return IntegerRedisMessage(a.size.toLong())
//        }
//    }




    //获取集合的成员数√√√√√√√√√√√√√√√√√√√√√√√√
    fun scard(array: List<String>):RedisMessage {
        val key = array[1]
        val array = array-array[0]-array[1]
        if(rset.containsKey(key)){
            return IntegerRedisMessage(rset[key]!!.size.toLong())
        }
        else return IntegerRedisMessage(0)
    }

    //返回第一个集合与其他集合之间的差异。~~~~~~~~~~
    fun sdiff(array: List<String>):RedisMessage {
        val key = array[1]
        val array = array-array[0]-array[1]
        var set = rset[key] //把key赋值给set
            array.forEach {
                if (rset[it] == null){
                    return SimpleStringRedisMessage("null1+$it")
                }
                set = (rset[it]?.let { it1 -> set?.subtract(it1) })as kotlin.collections.MutableSet<String>
                if(set == null){
                    return SimpleStringRedisMessage("null")
                }
        }

        //循环遍历arr数组，取出数据，为了读取rset[]。然后一个一个substractset就是最后差异
        var set1 = mutableListOf<RedisMessage>()
        set!!.forEach{ set1.add(SimpleStringRedisMessage(it))}                  //为打印做准备
        return ArrayRedisMessage(set1)
    }

    //返回给定所有集合的差集并存储在 destination 中~~~~~~~~~~
    fun sdiffstore(array: List<String>):RedisMessage {
        val key = array[1]
        val array = array-array[0]-array[1]
        var set = mutableSetOf<String>()
        array.forEach { set.plus(set.subtract(rset[it]!!))}  //差集  空集和别的差集就是别的集合
        rset.put(key, set)
        return IntegerRedisMessage(set.size.toLong())
    }

    //返回给定所有集合的交集!!!!!!!!!!!!
    fun sinter(array: List<String>):RedisMessage{
        val key = array[1]
        val array = array-array[0]-array[1]
        var set = setOf1<String>()
        val set1 = mutableListOf<RedisMessage>()
        for ( i in 0..array.size){
            set = set.plus(set.intersect(rset[array[i]]!!))
        }
        set.forEach { set1.add(SimpleStringRedisMessage(it)) }
        return ArrayRedisMessage(set1)
    }

    //返回给定所有集合的交集并存储在 destination 中!!!!!!!!!!!!
    fun sinterstore(array: List<String>):RedisMessage {
        val key = array[1]
        val array = array-array[0]-array[1]
//        val a = !rset.containsKey(key)  //提前判断key是否为空
        var set = setOf1<String>()

        if (!rset.containsKey(key)) {
            array.forEach { set = set.intersect(rset[it]!!) }
            rset.put(key, set as kotlin.collections.MutableSet<String>)
        }else{
            array.forEach { set = set.intersect(rset[it]!!) }
            rset[key] = rset[key]?.plus(set) as kotlin.collections.MutableSet<String>
        }
        return IntegerRedisMessage(set.size.toLong())
    }

    //判断 member 元素是否是集合 key 的成员√√√√√√√√√√√√√√√√√√√√√
    fun sismember(array: List<String>):RedisMessage{
        val key = array[1]
        val array = array-array[0]-array[1]
        val newarray = rset[key]?.minus(array[0])
        if( newarray?.size != rset[key]!!.size ){
            return IntegerRedisMessage(1)
        }else return IntegerRedisMessage(0)
    }

    //返回集合中的所有成员√√√√√√√√√√√√√√√√√√√√√√
    fun smembers(array: List<String>):RedisMessage{
        val key = array[1]
        val array = array-array[0]-array[1]
        if(!rset.containsKey(key)){
            rset.put(key,setOf1<String>() as MutableSet1<String>)
            return SimpleStringRedisMessage("(empty list or set)")
        }else {
            val new = mutableListOf<RedisMessage>()
            rset[key]?.forEach { new.add(SimpleStringRedisMessage(it)) }
            return ArrayRedisMessage(new)
        }
    }

    //将 member 元素从 source 集合移动到 destination 集合!!!!!!!!!!!
    fun smove(array: List<String>):RedisMessage{
        val key = array[1]
        val array = array-array[0]-array[1]
        if(rset[key]!!.contains(array[1])){
            rset[key]?.remove(array[1])
            rset[array[0]]?.add(array[1])
            return IntegerRedisMessage(1)
        }else return IntegerRedisMessage(0)
    }
    //移除并返回集合中的一个随机元素√√√√√√√√√√√√√√√√√√
    fun spop(array: List<String>):RedisMessage {
        val key = array[1]
        if (rset[key] != null) {
            val random = rset[key]?.random() //确保两次是一个数字
            rset[key]!!.remove(random)
            return SimpleStringRedisMessage(random)
        } else {
            return SimpleStringRedisMessage("nil")
        }
    }


    //返回集合中一个或多个随机数√√√√√√√√√√√√√√√√√√√√√√√√√√√√
    fun srandmembery(array: List<String>):RedisMessage{
        val key = array[1]
        val array = array-array[0]-array[1]
        if(!rset.containsKey(key)){
            return SimpleStringRedisMessage("nil")
        }
         if (array[0].toInt() < rset[key]!!.size || array[0].toInt() > 0 || rset[key] != null ) {
             var list = listOf<String>()
             var rset = rset[key]
             for (i in 1..array[0].toInt()) {
                 val random = rset!!.random()
                 rset?.minus(random)
                 list = list + random
            }
             val rmList = mutableListOf<RedisMessage>()
             list.forEach { rmList.add(SimpleStringRedisMessage(it)) }
             return ArrayRedisMessage(rmList)
        }else if (array[0].toInt() < rset[key]!!.size || array[0].toInt() < 0 || rset[key] != null) {
            var list = listOf<String>()
             var rset = rset[key]
             for (i in 1..array[0].toInt().absoluteValue) {
                 val random = rset!!.random()
                 list = list + random
             }
             val rmList = mutableListOf<RedisMessage>()
             list.forEach { rmList.add(SimpleStringRedisMessage(it)) }
             return ArrayRedisMessage(rmList)
        } else {
            return SimpleStringRedisMessage("咋取0啊")
        }
    }
    //移除集合中一个或多个成员√√√√√√√√√√√√√√√√
    fun srem(array: List<String>):RedisMessage{
        val key = array[1]
        val array = array-array[0]-array[1]
        //类型？
        var a = 0
        array.forEach{
                if (rset[key]!!.contains(it)){
                    rset[key]!!.remove(it)
                    ++a
            }
        }
        return IntegerRedisMessage(a.toLong())
    }


    //返回所有给定集合的并集
    fun sunion(array: List<String>): RedisMessage {
        val key = array[1]
        val array = array-array[0]-array[1]
        if (!rset.containsKey(key)) {
            rset[key] == null
        }
        if (!rset.containsKey(array[0])){
            rset[array[0]] == null
        }
        val list = rset[key]!!.plus(rset[array[0]]!!)
        val rmList = mutableListOf<RedisMessage>()
        list.forEach { rmList.add(SimpleStringRedisMessage(it)) }
        return ArrayRedisMessage(rmList)
    }

    //所有给定集合的并集存储在 destination 集合中
    fun sunionstore(array: List<String>):RedisMessage{
        val key = array[1]
        val array = array-array[0]-array[1]
        var set = mutableSetOf<String>()
        array.forEach {
            set.plus(rset[it]!!)
        }
        if (!rset.containsKey(array[0])) {
            rset.put(key,set)
        } else {
            rset[key]!!.plus(set)
        }
        return  IntegerRedisMessage(set.size.toLong())
    }


    //迭代集合中的元素
//    fun sscan(array: List<String>){
//
//    }

    fun operation(command: String, array: List<String>):RedisMessage {
        return when (command) {
            "SADD" -> sadd(array)
            "SCARD" -> scard(array)
            "SDIFF" -> sdiff(array)
            "SDIFFSTORE" -> sdiffstore(array)
            "SINTER" -> sinter(array)
            "SINTERSTORE" -> sinterstore(array)
            "SISMEMBER" -> sismember(array)//空?
            "SMEMBERS" -> smembers(array)
            "SMOVE" -> smove(array)
            "SPOP" -> spop(array)
            "SRANDMEMBER" -> srandmembery(array)
            "SREM" -> srem(array)//类型怎么求
            "SUNION" -> sunion(array)
            "SUNIONSTORE" -> sunionstore(array)
//              "SSCAN" -> setnx(key,other)//数学方法
            else -> ErrorRedisMessage("not support this command at present")
        }
    }
}
