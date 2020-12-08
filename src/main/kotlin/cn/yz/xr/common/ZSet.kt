package cn.yz.xr.common

class ZSet(
    var zSet: Any,
    var operationList: List<String> = listOf("ZADD")
){
    fun operation(command: String, array: List<String>):String{
        return ""
    }
}