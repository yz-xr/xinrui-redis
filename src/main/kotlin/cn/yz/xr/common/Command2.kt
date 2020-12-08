package cn.yz.xr.common

import java.nio.channels.SocketChannel
import java.util.*

data class Command2(
        val time: Long,
        val content: Array<String>,
        val socketChannel: SocketChannel
)