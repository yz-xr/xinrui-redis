package cn.yz.xr.common

class Rset(
    var rset: HashMap<String,Set<String>> = hashMapOf(),
    var operationList: List<String> = listOf("")
){

    fun operation(command: String, array: List<String>):String{
        return ""
    }
}