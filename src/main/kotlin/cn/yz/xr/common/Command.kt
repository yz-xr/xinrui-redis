package cn.yz.xr.common

import akka.actor.typed.ActorRef
import java.util.*

data class Command(
        val time: Date,
        val content: String,
        val clientId: Int,
        val father: ActorRef<Any>
)