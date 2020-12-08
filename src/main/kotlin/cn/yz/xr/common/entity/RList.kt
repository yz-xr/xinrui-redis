package cn.yz.xr.common.entity

class RList(
    var list: Map<String,List<String>> = mapOf(),
    var operationList: List<String> = listOf("LSET","LGET","LSETNX")
){
    fun operation(command: String, array: List<String>):String{
        return ""
    }
}