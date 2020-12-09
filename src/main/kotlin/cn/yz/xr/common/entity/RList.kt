package cn.yz.xr.common.entity

import cn.yz.xr.common.utils.MessageUtil
import io.netty.handler.codec.redis.*
import kotlin.math.absoluteValue

class RList(
        var listMap: MutableMap<String, ArrayDeque<String>> = mutableMapOf(),
        var operationList: List<String> = listOf("LSET", "LPUSH", "LPOP", "RPUSH", "RPOP", "LLEN", "LINDEX", "LRANGE", "LREM")
) {

    private fun lpush(array: List<String>): RedisMessage {

        if (array.size <= 2) {
            return ErrorRedisMessage("wrong number of arguments for 'lpush' command")
        }

        var originalLength = 0
        if (listMap.containsKey(array[1])) {
            val exitedDeque: ArrayDeque<String>? = listMap[array[1]]
            if (exitedDeque != null) {
                originalLength = exitedDeque.size + array.size - 2
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
            originalLength = array.size - 2
        }
        return IntegerRedisMessage(originalLength.toLong())
    }

    private fun rpush(array: List<String>): RedisMessage {

        if (array.size <= 2) {
            return ErrorRedisMessage("wrong number of arguments for 'lpush' command")
        }

        var originalLength = 0
        if (listMap.containsKey(array[1])) {
            val exitedDeque: ArrayDeque<String>? = listMap[array[1]]
            if (exitedDeque != null) {
                originalLength = exitedDeque.size + array.size - 2
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
            originalLength = array.size - 2
        }
        return IntegerRedisMessage(originalLength.toLong())
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

    private fun lrange(array: List<String>): RedisMessage {
        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 4)

        if (paramError == null) {
            return if (listMap.containsKey(array[1])) {
                val existedDeque = listMap[array[1]]
                val intTransErrorFor2 = MessageUtil.checkString2Int(array[2])
                val intTransErrorFor3 = MessageUtil.checkString2Int(array[3])
                return if (intTransErrorFor2 == null && intTransErrorFor3 == null) {
                    if (existedDeque != null) {
                        val low = array[2].toInt()
                        val high = array[3].toInt()
                        val lowForSubIndex: Int = when {
                            low < 0 && low >= (0 - existedDeque.size) -> low + existedDeque.size
                            low < (0 - existedDeque.size) -> 0
                            else -> low
                        }
                        val highForSubIndex: Int = when {
                            high <= existedDeque.size - 1 && high >= 0 -> high + 1
                            high > existedDeque.size - 1 -> existedDeque.size
                            high < 0 && high >= (0 - existedDeque.size) -> high + existedDeque.size + 1
                            else -> 0
                        }
                        if (lowForSubIndex > highForSubIndex) {
                            return SimpleStringRedisMessage("(empty list or set)")
                        }
                        val subList = existedDeque.subList(lowForSubIndex, highForSubIndex)
                        val rmList = mutableListOf<RedisMessage>()
                        subList.forEach { rmList.add(SimpleStringRedisMessage(it)) }
                        ArrayRedisMessage(rmList)
                    } else {
                        SimpleStringRedisMessage("(empty list or set)")
                    }
                } else {
                    ErrorRedisMessage("ERR value is not an integer or out of range")
                }
            } else {
                SimpleStringRedisMessage("(empty list or set)")
            }
        } else {
            return paramError
        }
    }

    private fun lrem(array: List<String>): RedisMessage {
        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 4)

        if (paramError == null) {
            return if (listMap.containsKey(array[1])) {
                val existedDeque = listMap[array[1]]
                val intTransError = MessageUtil.checkString2Int(array[2])
                return if (intTransError != null) {
                    intTransError
                } else {
                    val count = array[2].toInt()
                    if (existedDeque != null) {
                        when {
                            count > 0 -> {
                                // count > 0 : 从表头开始向表尾搜索，移除与 VALUE 相等的元素，数量为 COUNT
                                var num = 0
                                existedDeque.forEach {
                                    if (it == array[3] && num < count) {
                                        existedDeque.remove(it)
                                        num++
                                    }
                                }
                                IntegerRedisMessage(num.toLong())
                            }
                            count < 0 -> {
                                // count < 0 : 从表尾开始向表头搜索，移除与 VALUE 相等的元素，数量为 COUNT 的绝对值
                                var num = 0
                                val existedDequeReversed = existedDeque.asReversed()
                                existedDequeReversed.reversed().forEach {
                                    if (it == array[3] && num < count.absoluteValue) {
                                        existedDequeReversed.remove(it)
                                        num++
                                    }
                                }
                                existedDequeReversed.asReversed()
                                IntegerRedisMessage(num.toLong())
                            }
                            else -> {
                                // count = 0 : 移除表中所有与 VALUE 相等的值
                                val beforeNum = existedDeque.size
                                existedDeque.removeIf { it == array[3] }
                                IntegerRedisMessage((beforeNum - existedDeque.size).toLong())
                            }
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

    fun operation(command: String, array: List<String>): RedisMessage {
        return when (command) {
            "LSET" -> lset(array)
            "LPUSH" -> lpush(array)
            "LPOP" -> lpop(array)
            "RPUSH" -> rpush(array)
            "RPOP" -> rpop(array)
            "LLEN" -> llen(array)
            "LINDEX" -> llindex(array)
            "LRANGE" -> lrange(array)
            "LREM" -> lrem(array)
            else -> ErrorRedisMessage("not support this command at present")
        }
    }
}