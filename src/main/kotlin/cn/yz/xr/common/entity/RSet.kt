package cn.yz.xr.common.entity

class RSet(
    var rset: HashMap<String,Set<String>> = hashMapOf(),
    var operationList: List<String> = listOf("")
){

    fun operation(command: String, array: List<String>):String{
        return ""
    }
}