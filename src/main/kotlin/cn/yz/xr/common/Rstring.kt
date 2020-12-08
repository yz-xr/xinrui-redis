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

    private fun setnx(key:String, value: String):String{
        return if(map.containsKey(key)){
            "(integer) 0"
        }else{
            map[key] = value
            "OK"
        }
    }

    private fun getset(key:String, value: String):String{
        var res = "(nil)"
        if(map.containsKey(key)){
            res = map[key]?:"(nil)"
            map[key] = value
        }else{
            map[key] = value
        }
        return res
    }

    private fun strlen(key:String):String{
        return "(integer) ${map[key]?:"".length}"
    }

    fun append(key:String, value: String):String{
        map[key] = map[key]?:"" + value
        return "(integer) ${map[key]?:"".length}"
    }

    fun setrange(key: String, offset:Int, value: String):String{
        return ""
    }

    fun operation(command: String):String{
        var array = command.split(" ")
        var type = array[0].toUpperCase()
        return when(type){
            "GET" -> get(array[1])
            "SET" -> set(array[1], array[2])
            "SETNX" -> setnx(array[1], array[2])
            "GETSET" -> getset(array[1], array[2])
            "STRLEN" -> strlen(array[1])
            "APPEND" -> append(array[1], array[2])
            else -> "not supported command"
        }
    }
}