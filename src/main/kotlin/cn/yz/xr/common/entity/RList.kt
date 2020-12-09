package cn.yz.xr.common.entity

import cn.yz.xr.common.utils.MessageUtil
import io.netty.handler.codec.redis.*
import kotlin.math.absoluteValue

class RList(
        private var listMap: MutableMap<String, ArrayDeque<String>> = mutableMapOf(),
        var operationList: List<String> = listOf("LSET", "LPUSH", "LPOP", "RPUSH", "RPOP", "LLEN", "LINDEX", "LRANGE", "LREM")
) {

    private fun lpush(array: List<String>): RedisMessage {

        if (array.size <= 2) {
            return ErrorRedisMessage("wrong number of arguments for 'lpush' command")
        }

        if (listMap.containsKey(array[1])) {
            val exitedDeque: ArrayDeque<String>? = listMap[array[1]]
            if (exitedDeque != null) {
                for (i in 2 until array.size) {
                    exitedDeque.addFirst(array[i])
                }
            } else {
                return ErrorRedisMessage("Inner exception")
            }
        } else {
            val deque: ArrayDeque<String> = ArrayDeque<String>()
            for (i in 2 until array.size) {
                deque.addFirst(array[i])
            }
            listMap[array[1]] = deque
        }
        return IntegerRedisMessage((array.size - 2).toLong())
    }

    private fun rpush(array: List<String>): RedisMessage {

        if (array.size <= 2) {
            return ErrorRedisMessage("wrong number of arguments for 'lpush' command")
        }

        if (listMap.containsKey(array[1])) {
            val exitedDeque: ArrayDeque<String>? = listMap[array[1]]
            if (exitedDeque != null) {
                for (i in 2 until array.size) {
                    exitedDeque.addLast(array[i])
                }
            } else {
                return ErrorRedisMessage("Inner exception")
            }
        } else {
            val deque: ArrayDeque<String> = ArrayDeque<String>()
            for (i in 2 until array.size) {
                deque.addLast(array[i])
            }
            listMap[array[1]] = deque
        }
        return IntegerRedisMessage((array.size - 2).toLong())
    }

    private fun lpop(array: List<String>): RedisMessage {
        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 2)
        if (paramError == null) {
            if (listMap.containsKey(array[1])) {
                val existedDeque = listMap[array[1]]
                return if (existedDeque != null) {
                    val removeEle: String? = existedDeque.removeFirstOrNull()
                    return if (removeEle != null) {
                        SimpleStringRedisMessage(removeEle)
                    } else {
                        SimpleStringRedisMessage("(empty list or set)")
                    }
                } else {
                    ErrorRedisMessage("Inner exception")
                }
            }
            return SimpleStringRedisMessage("(nil)")
        } else {
            return paramError
        }
    }

    private fun rpop(array: List<String>): RedisMessage {
        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 2)

        if (paramError == null) {
            if (listMap.containsKey(array[1])) {
                val existedDeque = listMap[array[1]]
                return if (existedDeque != null) {
                    val removeEle: String? = existedDeque.removeLastOrNull()
                    return if (removeEle != null) {
                        SimpleStringRedisMessage(removeEle)
                    } else {
                        SimpleStringRedisMessage("(empty list or set)")
                    }
                } else {
                    ErrorRedisMessage("Inner exception")
                }
            }
            return SimpleStringRedisMessage("(nil)")
        } else {
            return paramError
        }
    }

    private fun lset(array: List<String>): RedisMessage {
        // 参数数目校验
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 4)

        if (paramError == null) {
            if (listMap.containsKey(array[1])) {
                val existedDeque = listMap[array[1]]
                val intTransError = MessageUtil.checkString2Int(array[2])
                return if (intTransError != null) {
                    intTransError
                } else {
                    val index = array[2].toInt()
                    val value = array[3]
                    if (existedDeque != null) {
                        when (index) {
                            in (0 until existedDeque.size) -> {
                                existedDeque[index]
                                SimpleStringRedisMessage("OK")
                            }
                            in ((0 - existedDeque.size) until 0) -> {
                                existedDeque[existedDeque.size + index] = value
                                SimpleStringRedisMessage("OK")
                            }
                            else -> ErrorRedisMessage("ERR index out of range")
                        }
                    } else {
                        ErrorRedisMessage("Inner exception")
                    }
                }
            } else {
                return ErrorRedisMessage("ERR no such key")
            }
        } else {
            return paramError
        }
    }

    private fun llen(array: List<String>): RedisMessage {
        // 参数数目校验
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 2)

        if (paramError == null) {
            return if (listMap.containsKey(array[1])) {
                val existedDeque = listMap[array[1]]
                if (existedDeque != null) {
                    IntegerRedisMessage(existedDeque.size.toLong())
                } else {
                    ErrorRedisMessage("Inner exception")
                }
            } else {
                IntegerRedisMessage(0)
            }
        } else {
            return paramError
        }
    }

    private fun llindex(array: List<String>): RedisMessage {
        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 3)

        if (paramError == null) {
            return if (listMap.containsKey(array[1])) {
                val existedDeque = listMap[array[1]]
                val intTransError = MessageUtil.checkString2Int(array[2])
                return if (intTransError != null) {
                    intTransError
                } else {
                    val index = array[2].toInt()
                    if (existedDeque != null) {
                        if (index.absoluteValue > existedDeque.size - 1) {
                            SimpleStringRedisMessage("(nil)") // 索引超过
                        } else {
                            // 将index设置为数组上界
                            SimpleStringRedisMessage(existedDeque[index.absoluteValue])
                        }
                    } else {
                        ErrorRedisMessage("Inner exception")
                    }
                }
            } else {
                IntegerRedisMessage(0)
            }
        } else {
            return paramError
        }
    }

//    private fun lrange(array: List<String>): RedisMessage {
//        // 校验参数个数
//        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 4)
//
//        if (paramError == null) {
//            return if (listMap.containsKey(array[1])) {
//                val existedDeque = listMap[array[1]]
//                val intTransErrorFor2 = MessageUtil.checkString2Int(array[2])
//                val intTransErrorFor3 = MessageUtil.checkString2Int(array[3])
//                return if (intTransErrorFor2 != null || intTransErrorFor3 != null) {
//                    intTransErrorFor2
//                } else {
//                    if (existedDeque != null) {
//                        var low = array[2].toInt()
//                        var high = array[3].toInt()
//                        if (low < 0) low = 0
//                        if (high > existedDeque.size - 1) high = existedDeque.size - 1
//                        val subList = existedDeque.subList(low, high)
//                        val rmList = mutableListOf<RedisMessage>()
//                        subList.forEach { rmList.add(SimpleStringRedisMessage(it)) }
//                        ArrayRedisMessage(rmList)
//                    } else {
//                        SimpleStringRedisMessage("(empty list or set)")
//                    }
//                }
//            } else {
//                ErrorRedisMessage("Inner exception")
//            }
//        } else {
//            return paramError
//        }
//    }

    private fun lrem(array: List<String>): RedisMessage {
        // 校验参数个数
        MessageUtil.checkArgsNum(array, 4)

        return if (listMap.containsKey(array[1])) {
            val existedDeque = listMap[array[1]]
            if (MessageUtil.checkString2Int(array[2]) == null) {
                ErrorRedisMessage("ERR value is not an integer or out of range")
            } else {
                val count = array[2].toInt()
                if (existedDeque != null) {
                    if (count.absoluteValue > 0) {
                        // 删除指定个数匹配的key
                        existedDeque.removeIf { it == array[3] }
                        IntegerRedisMessage(1)
                    } else {
                        // 当为0时删除所有的匹配的key
                        existedDeque.removeIf { it == array[3] }
                        IntegerRedisMessage(1)
                    }
                } else {
                    ErrorRedisMessage("Inner exception")
                }
            }
        } else {
            IntegerRedisMessage(0)
        }

    }

    fun operation(command: String, array: List<String>): RedisMessage {
        return when (command) {
            "LSET" -> lset(array)
            "LPUSH" -> lpush(array)
            "LPOP" -> lpop(array)
            "RPUSH" -> rpush(array)
            "RPOP" -> rpop(array)
            "LLEN" -> llen(array)
            "LINDEX" -> llindex(array)
            //"LRANGE" -> lrange(array)
            //"LREM" -> lrem(array)
            else -> ErrorRedisMessage("not support this command at present")
        }
    }
}