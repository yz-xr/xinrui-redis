package cn.yz.xr.producer.communication

import akka.actor.typed.ActorRef

/**
 * 子actor向父actor请求兄弟数组的数据类
 */
data class QueryBrother(val replyTo: ActorRef<Any>)