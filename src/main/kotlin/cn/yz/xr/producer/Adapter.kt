package cn.yz.xr.producer


fun main(args: Array<String>){
//    val demoMain: ActorSystem<Any> = ActorSystem.create(ManagerActor.create(10), "ManagerActor")
//    println("请输入命令：")
//    var content = readLine()
//    while (content!="quit"){
//        val command = content?.let { Command(Date(), it, 13, demoMain) }
//        demoMain.tell(command)
//        println("请输入命令：")
//        content = readLine()
//    }
//    demoMain.terminate()

    var str = "hello, my friend"
    var map = mapOf<Int,Int>(0 to 3, 0 to -1, -3 to -1)
    var start = 0
    var end = -1
//    for(p in map){
//        println(str.substring(p.key,p.value))
//    }
//    var number = "0001111".toInt()
//    println(number)
    var list = listOf<String>("123","-123","abc", "0")
    for(l in list){
        var value = l.toIntOrNull()
        if(value==null){
            println("(error) ERR value is not an integer or out of range")
        }else{
            println("(integer) ${value + 1}")
        }
    }
}