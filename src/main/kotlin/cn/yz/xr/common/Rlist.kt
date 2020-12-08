package cn.yz.xr.common

class Rlist(
    var list: Map<String,List<String>> = mapOf(),
    var operationList: List<String> = listOf("LSET","LGET","LSETNX")
){
    fun operation(command: String, array: List<String>):String{
        return ""
    }
}