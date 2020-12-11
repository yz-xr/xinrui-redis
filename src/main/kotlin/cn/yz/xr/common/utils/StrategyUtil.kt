package cn.yz.xr.common.utils

import akka.actor.typed.ActorRef
import kotlin.math.abs

/**
 * author:雷克萨
 */

class StrategyUtil{
    companion object{
        /**
         * 调度子actor的策略，可定制化
         */
        fun scheduleActor(key:String, childArray:ArrayList<ActorRef<Any>>):ActorRef<Any>{
            return childArray[abs(key.hashCode() % childArray.size)]
        }
    }
}