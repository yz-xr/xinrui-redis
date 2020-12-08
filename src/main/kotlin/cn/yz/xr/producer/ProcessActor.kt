package producer

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import cn.yz.xr.common.*

class ProcessActor(
    context: ActorContext<Command>,
    private var rString:Rstring = Rstring(),
    private var rList: Rlist = Rlist(),
    private var rHash: Rhash = Rhash(),
    private var rSet: Rset = Rset(),
    private var rZSet: ZSet = ZSet("hello")
) : AbstractBehavior<Command> (context){

    companion object{
        fun create(): Behavior<Command> {
            return Behaviors.setup{
                    context: ActorContext<Command> ->
                ProcessActor(context)
            }
        }
    }

    override fun createReceive(): Receive<Command> {
        return newReceiveBuilder()
            .onMessage(
                Command::class.java
            ) { command:  Command ->
                onProcess(command)
            }.build()
    }

    private fun onProcess(command: Command): Behavior<Command>{
        var (time, content, client, father) = command
        var operation = content.split(" ")[0].toUpperCase()
        var response = when(operation){
            in this.rString.operationList -> this.rString.operation(content)
            in this.rList.operationList -> this.rList.operation(content)
            in this.rHash.operationList -> this.rHash.operation(content)
            in this.rSet.operationList -> this.rSet.operation(content)
            in this.rZSet.operationList -> this.rZSet.operation(content)
            else -> otherProcess(content)
        }
        father.tell(response)
        return this
    }

    private fun otherProcess(command: String) : String{
        return "not support command"
    }
}