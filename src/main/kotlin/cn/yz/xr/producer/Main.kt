package producer

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import cn.yz.xr.common.Command


fun main(args:Array<String>){
    // 子Actor的个数
    var childrenActorNum = 10

    val demoMain: ActorSystem<Any> = ActorSystem.create(ManagerActor.create(childrenActorNum), "ManagerActor")

    // 存储子Actor引用的数组
    demoMain.tell("hello")
}