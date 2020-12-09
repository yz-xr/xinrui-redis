package cn.yz.xr.common.utils

import akka.actor.typed.ActorRef

class StrategyUtil{
    companion object{
        /**
         * 调度子actor的策略，可定制化
         */
        fun scheduleActor(key:String, childArray:ArrayList<ActorRef<Any>>):ActorRef<Any>{
            return childArray[key.hashCode() % childArray.size]
        }
    }
}