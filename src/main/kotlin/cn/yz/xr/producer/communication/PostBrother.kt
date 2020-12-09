package cn.yz.xr.producer.communication

import akka.actor.typed.ActorRef

/**
 * 父actor向子actor返回兄弟列表的数据类
 */
data class PostBrother(val brother: ArrayList<ActorRef<Any>>)