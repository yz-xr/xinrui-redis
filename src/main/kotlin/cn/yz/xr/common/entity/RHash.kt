package cn.yz.xr.common.entity

import cn.yz.xr.common.utils.MessageUtil
import io.netty.handler.codec.redis.*

class RHash(
        var listMap: MutableMap<String, MutableMap<String, Any>> = mutableMapOf<String, MutableMap<String, Any>>(),
        val operationList: List<String> = listOf("HSET", "HGET", "HGETALL", "HDEL", "HSETNX", "HLEN", "HMSET", "HMGET", "HINCRBY", "HEXISTS")
) {

    private fun hset(array: List<String>): RedisMessage {
        if (array.size <= 3) {
            return ErrorRedisMessage("wrong number of arguments for 'hset' command")
        }

        if (listMap.containsKey(array[1])) {
            val existedMap = listMap[array[1]]
            return if (existedMap != null) {
                var retNum = 0
                // hset obj key1 v1 key2 v2
                for (i in 2 until array.size step 2) {
                    if (!existedMap.containsKey(array[i])) {
                        retNum++
                    }
                    existedMap[array[i]] = array[i + 1]
                }
                SimpleStringRedisMessage(retNum.toString())
            } else {
                ErrorRedisMessage("Inner exception")
            }
        } else {
            val createdMap = HashMap<String, Any>()
            for (i in 2 until array.size step 2) {
                createdMap[array[i]] = array[i + 1]
            }
            listMap[array[1]] = createdMap
            return SimpleStringRedisMessage(createdMap.size.toString())
        }
    }

    private fun hget(array: List<String>): RedisMessage {
        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 3)

        if (paramError == null) {
            return if (listMap.containsKey(array[1])) {
                val existedMap = listMap[array[1]]
                if (existedMap != null) {
                    if (existedMap.containsKey(array[2])) {
                        SimpleStringRedisMessage(existedMap[array[2]].toString())
                    } else {
                        SimpleStringRedisMessage("(nil)")
                    }
                } else {
                    ErrorRedisMessage("Inner Exception")
                }
            } else {
                SimpleStringRedisMessage("(nil)")
            }
        } else {
            return paramError
        }
    }

    private fun hmget(array: List<String>): RedisMessage {
        // 校验参数个数
        if (array.size <= 2) {
            return ErrorRedisMessage("wrong number of arguments for 'hmget' command")
        }

        val rmList: MutableList<RedisMessage> = mutableListOf()
        return if (listMap.containsKey(array[1])) {
            val existedMap = listMap[array[1]]
            if (existedMap != null) {
                for (i in 2 until array.size) {
                    if (existedMap.containsKey(array[2])) {
                        rmList.add(SimpleStringRedisMessage(existedMap[array[2]].toString()))
                    } else {
                        rmList.add(SimpleStringRedisMessage("(nil)"))
                    }
                }
                ArrayRedisMessage(rmList)
            } else {
                ErrorRedisMessage("Inner Exception")
            }
        } else {
            for (i in 0 until array.size - 2) {
                rmList.add(SimpleStringRedisMessage("(nil)"))
            }
            ArrayRedisMessage(rmList)
        }

    }

    private fun hexists(array: List<String>): RedisMessage {
        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 3)

        if (paramError == null) {
            return if (listMap.containsKey(array[1])) {
                val existedMap = listMap[array[1]]
                if (existedMap != null) {
                    if (existedMap.containsKey(array[2])) {
                        IntegerRedisMessage(1.toLong())
                    } else {
                        IntegerRedisMessage(0)
                    }
                } else {
                    ErrorRedisMessage("Inner Exception")
                }
            } else {
                IntegerRedisMessage(0)
            }
        } else {
            return paramError
        }
    }

    private fun hlen(array: List<String>): RedisMessage {
        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 2)

        if (paramError == null) {
            return if (listMap.containsKey(array[1])) {
                val existedMap = listMap[array[1]]
                if (existedMap != null) {
                    IntegerRedisMessage(existedMap.size.toLong())
                } else {
                    ErrorRedisMessage("Inner Exception")
                }
            } else {
                IntegerRedisMessage(0)
            }
        } else {
            return paramError
        }
    }

    private fun hsetnx(array: List<String>): RedisMessage {
        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 4)

        if (paramError == null) {
            if (listMap.containsKey(array[1])) {
                val existedMap = listMap[array[1]]
                return if (existedMap != null) {
                    // hsetnx obj key1 v1
                    if (!existedMap.containsKey(array[2])) {
                        existedMap[array[2]] = array[3]
                        IntegerRedisMessage(1)
                    } else {
                        IntegerRedisMessage(0)
                    }
                } else {
                    ErrorRedisMessage("Inner exception")
                }
            } else {
                val createdMap = LinkedHashMap<String, Any>()
                createdMap[array[2]] = array[3]
                listMap[array[1]] = createdMap
                return IntegerRedisMessage(1)
            }
        } else {
            return paramError
        }
    }

    private fun hdel(array: List<String>): RedisMessage {

        if (array.size <= 2) {
            return ErrorRedisMessage("wrong number of arguments for 'hdel' command")
        }

        if (listMap.containsKey(array[1])) {
            val existedMap = listMap[array[1]]
            return if (existedMap != null) {
                var retNum = 0
                for (i in 2 until array.size) {
                    if (existedMap.containsKey(array[i])) {
                        existedMap.remove(array[i])
                        retNum++
                    }
                }
                IntegerRedisMessage(retNum.toLong())
            } else {
                ErrorRedisMessage("Inner exception")
            }
        } else {
            return IntegerRedisMessage(0)
        }
    }

    private fun hgetall(array: List<String>): RedisMessage {
        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 2)

        if (paramError == null) {
            if (listMap.containsKey(array[1])) {
                val existedMap = listMap[array[1]]
                return if (existedMap != null) {
                    val rmList = mutableListOf<RedisMessage>()
                    for ((key, value) in existedMap) {
                        rmList.add(SimpleStringRedisMessage(key))
                        rmList.add(SimpleStringRedisMessage(value.toString()))
                    }
                    return ArrayRedisMessage(rmList)
                } else {
                    ErrorRedisMessage("Inner exception")
                }
            } else {
                return SimpleStringRedisMessage("(empty list or set)")
            }
        } else {
            return IntegerRedisMessage(0)
        }
    }

    private fun hincrby(array: List<String>): RedisMessage {

        // 校验参数个数
        val paramError: ErrorRedisMessage? = MessageUtil.checkArgsNum(array, 4)

        if (paramError == null) {
            val intTransError = MessageUtil.checkString2Int(array[3])
            return if (intTransError != null) {
                intTransError
            } else {
                val numValue = array[3].toInt()
                return if (listMap.containsKey(array[1])) {
                    val existedMap = listMap[array[1]]
                    return if (existedMap != null) {
                        if (existedMap.containsKey(array[2])) {
                            val originalValue = existedMap[array[2]].toString()
                            val intTransErrorInner = MessageUtil.checkString2Int(originalValue)
                            return if (intTransErrorInner != null) {
                                intTransErrorInner
                            } else {
                                val newValue = originalValue.toInt() + numValue
                                existedMap[array[2]] = newValue
                                IntegerRedisMessage(newValue.toLong())
                            }
                        } else {
                            IntegerRedisMessage(0)
                        }
                    } else {
                        ErrorRedisMessage("Inner exception")
                    }
                } else {
                    // 不存在的key，新建后添加值
                    val createdMap = LinkedHashMap<String, Any>()
                    createdMap[array[2]] = numValue
                    listMap[array[1]] = createdMap
                    println(listMap)
                    IntegerRedisMessage(numValue.toLong())
                }
            }
        } else {
            return paramError
        }
    }

    fun operation(command: String, array: List<String>): RedisMessage {
        return when (command) {
            "HSET" -> hset(array)
            "HGET" -> hget(array)
            "HGETALL" -> hgetall(array)
            "HDEL" -> hdel(array)
            "HSETNX" -> hsetnx(array)
            "HLEN" -> hlen(array)
            "HMSET" -> hset(array)
            "HMGET" -> hmget(array)
            "HINCRBY" -> hincrby(array)
            "HEXISTS" -> hexists(array)
            else -> ErrorRedisMessage("not support this command at present")
        }
    }
}