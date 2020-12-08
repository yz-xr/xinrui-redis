package cn.yz.xr.common

class Rstring(
    var map: LinkedHashMap<String,String> = linkedMapOf(),
    var operationList: List<String> = listOf("SET","GET","SETNX")
){
    fun set(key:String,value:String):String{
        map[key] = value
        return "OK"
    }

    fun get(key:String):String{
        return if(map.isEmpty() || map[key]==null){
            "(nil)"
        }else{
            map[key]?:"error"
        }
    }

    fun setnx(key:String, value: String):String{
        return if(map.containsKey(key)){
            "(integer) 0"
        }else{
            map[key] = value
            "OK"
        }
    }

    fun operation(command: String):String{
        var array = command.split(" ")
        var type = array[0].toUpperCase()
        return when(type){
            "GET" -> get(array[1])
            "SET" -> set(array[1],array[2])
            "SETNX" -> setnx(array[1],array[2])
            else -> "not supported command"
        }
    }
}