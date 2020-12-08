package cn.yz.xr.producer

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import cn.yz.xr.common.Command
import cn.yz.xr.producer.ProcessActor

class ManagerActor(
        context: ActorContext<Any>,
        private val max: Int,
        private var childArray: ArrayList<ActorRef<Command>> = arrayListOf(),
        private var response: String = ""
) : AbstractBehavior<Any>(context){

    init {
        for (i in 0..max){
            this.childArray.add(i, context.spawn(ProcessActor.create(), "ProcessActor-${i}"))
        }
    }

    companion object{
        fun create(max:Int): Behavior<Any> {
            return Behaviors.setup{
                    context: ActorContext<Any> ->
                ManagerActor(context,max)
            }
        }
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder().onMessage<Command>(
            Command::class.java
        ) { message: Command -> this.onCommand(message) }
            .onMessage<String>(
                String::class.java
            ){message: String -> this.returnRes(message) }
            .build()
    }

    // 接受command命令，并分配给子actor处理
    private fun onCommand(command: Command):Behavior<Any>{
        val (time,content,clientId) = command
        childArray[clientId % max].tell(command)
        return this
    }

    // 接受子actor的s值返回
    private fun returnRes(response: String):Behavior<Any> {
        this.response = response
        println(response)
        return this
    }

    fun getChildrenArray():ArrayList<ActorRef<Command>>{
        return this.childArray
    }

}