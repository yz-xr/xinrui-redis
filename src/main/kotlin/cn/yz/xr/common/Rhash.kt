package cn.yz.xr.common

class Rhash(
    var hash: LinkedHashMap<String, String> = linkedMapOf(),
    val operationList: List<String> = listOf("HSET","HGET")
){
    fun getAllValue():String{
        var res = ""
        var count = 1
        if(hash.isEmpty()){
            return "(empty list or set)"
        }
        for(value in hash.values){
            res = "${res}${count}) \"${value}\"\n"
            count++
        }
        return res
    }

    fun getAllKeys():String{
        var res = ""
        var count = 1
        if(hash.isEmpty()){
            return "(empty list or set)"
        }
        for(key in hash.keys){
            res = "${res}${count}) \"${key}\"\n"
            count++
        }
        return res
    }

    fun deleteByKey(key:String):String{
        this.hash.remove(key)
        return "(integer) 1"
    }

    fun add(key:String, value:String):String{
        this.hash[key] = value
        return "(integer) ${hash.size}"
    }

    fun operation(command:String):String{
        var array = command.split(" ")
        val type = array[0].toUpperCase()
        return when(type){
            "HVALS" -> getAllValue()
            else -> "not support command"
        }
    }
}