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
        return map[key]?:"(nil)"
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
        var res = map[key]?:""
        if(res.length < offset){
            for(i in res.length..offset){
                res += "\\x00"
            }
            res = res + offset
        }else if(offset + value.length < res.length){
            res = res.substring(0,offset) + value + res.substring(offset+value.length, res.length)
        }else{
            res = res.substring(0,offset) + value
        }
        return res
    }

    fun getrange(key:String, start:Int, end:Int):String{
        if(end in 1 until start){
            return ""
        }
        return map[key]?:"".substring(start,end)
    }

    private fun incr(key: String):String{
        var value = map[key]?:"0".toIntOrNull()
        return if(value is Int){
            map[key] = "${value+1}"
            "(integer) ${map[key]}"
        }else{
            "(error) ERR value is not an integer or out of range"
        }
    }

    fun operation(command: String, array: List<String>):String{
        return when(command){
            "GET" -> get(array[1])
            "SET" -> set(array[1], array[2])
            "SETNX" -> setnx(array[1], array[2])
            "GETSET" -> getset(array[1], array[2])
            "STRLEN" -> strlen(array[1])
            "APPEND" -> append(array[1], array[2])
            "SETRANGE" -> setrange(array[1],array[2].toInt()?:0,array[3])
            "INCR" -> incr(array[1])
            "getrange" -> getrange(array[1],array[2].toInt(),array[3].toInt())
            else -> "not supported command"
        }
    }
}