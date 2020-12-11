package cn.yz.xr.common.utils

import cn.hutool.core.util.CharsetUtil
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.FullBulkStringRedisMessage

/**
 * 消息通信工具类
 * @author: lewy
 */
open class MessageUtil {
    companion object {

        /**
         * ArrayRedisMessage 转数组
         */
        fun convertToArray(content: ArrayRedisMessage): List<String> {
            val children = content.children()
            val res = mutableListOf<String>()
            for (o in children) {
                res.add((o as FullBulkStringRedisMessage).content().toString(CharsetUtil.CHARSET_UTF_8))
            }
            return res
        }

        /**
         * 检验传参数目与希望是否相等
         */
        fun checkArgsNum(array: List<String>, expectedSize: Int): ErrorRedisMessage? {
            if (expectedSize != array.size) {
                return ErrorRedisMessage("wrong number of arguments (given ${array.size}, expected $expectedSize)")
            }
            return null
        }
        /**
         * 判断一个字符串能否转为整形
         */
        fun checkString2Int(str: String): ErrorRedisMessage? {
            return try {
                str.toInt()
                null
            } catch (e: NumberFormatException) {
                ErrorRedisMessage("ERR value is not an integer or out of range")
            }
        }

    }
}