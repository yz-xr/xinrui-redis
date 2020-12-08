package cn.yz.xr.common.utils

import cn.hutool.core.util.CharsetUtil
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.FullBulkStringRedisMessage

open class MessageUtil{
    companion object{
        open fun convertToArray(content: ArrayRedisMessage):List<String>{
            var childs = content.children()
            var res = mutableListOf<String>()
            for(o in childs){
                res.add((o as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8))
            }
            return res
        }
    }
}