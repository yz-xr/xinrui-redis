package cn.yz.xr.common.utils

class SkipList {
    private var head: SkipListNode
    private var maxLevel: Int = 1
    private val random: java.util.Random = java.util.Random()
    var length: Int
    private val MAX_LENGTH = 32

    init {
        head = SkipListNode("", Int.MIN_VALUE, arrayOfNulls(MAX_LENGTH))
        length = 0
    }

    fun insert(value: String, score: Int) {
        // 向该节点后中插入新的节点，需要更新多层链表上的前后节点
        val newNode = SkipListNode(value, score)

        this.maxLevel = if (newNode.level.size > this.maxLevel) newNode.level.size else this.maxLevel
        val updates = arrayOfNulls<SkipListNode>(this.maxLevel)

        for (i in updates.indices) {
            updates[i] = this.head
        }

        var currentNode = head
        // 二分查找要插入的位置
        for (i in (this.maxLevel - 1) downTo 0) {
            // 如果检查节点的score小于要查找的值，则向右推进
            while (currentNode.level[i] != null && currentNode.level[i]!!.score < score) {
                currentNode = currentNode.level[i]!!
            }
            // 当向下推进的时候，需要记录当前节点
            updates[i] = currentNode
        }

        for (i in newNode.level.indices) {
            newNode.level[i] = updates[i]!!.level[i]
            updates[i]!!.level[i] = newNode
        }

        this.length++
    }

    fun deleteByScore(score: Int) {
        // 获取所有可能的要删除的前向节点
        val updates = arrayOfNulls<SkipListNode>(this.maxLevel)
        var currentNode = head
        // 二分查找要插入的位置
        for (i in (this.maxLevel - 1) downTo 0) {
            // 如果检查节点的score小于要查找的值，则向右推进
            while (currentNode.level[i] != null && currentNode.level[i]!!.score < score) {
                currentNode = currentNode.level[i]!!
            }
            // 当向下推进的时候，需要记录当前节点
            updates[i] = currentNode
        }

        // 对于所有可能的节点，如果其确实指向了该节点，则删除
        for (i in updates.indices) {
            if (updates[i] != null && updates[i]!!.level[i] != null && updates[i]!!.level[i]!!.score == score) {
                updates[i]!!.level[i] = updates[i]!!.level[i]!!.level[i]
            }
        }
        this.length--
    }

    fun search(score: Int): Pair<String, Boolean> {
        var currentNode = head

        // 二分查找
        for (i in (this.maxLevel - 1) downTo 0) {
            loop@ while (currentNode.level[i] != null) {
                when {
                    // 如果检查节点的score等于要查找的值，则直接返回
                    currentNode.level[i]!!.score == score -> {
                        return Pair(currentNode.level[i]!!.value, true)
                    }
                    // 如果检查节点的score小于要查找的值，则向右推进
                    currentNode.level[i]!!.score < score -> {
                        currentNode = currentNode.level[i]!!
                    }
                    // 否则则向下推进
                    else -> break@loop
                }
            }
        }
        return Pair("", false)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("head: SkipList(head=$head, maxLevel=$maxLevel, random=$random, length=$length\n")
        for (i in this.maxLevel - 1 downTo 0) {
            val line = StringBuilder()
            var currentNode = this.head
            sb.append("$i layer: ")
            while (currentNode.level[i] != null) {
                line.append(" -> " + currentNode.level[i]!!.value)
                currentNode = currentNode.level[i]!!
            }
            sb.append(line.toString() + "\n")
        }
        sb.append("\n")
        return sb.toString()
    }

    inner class SkipListNode(value: String, score: Int) {
        val value: String = value
        val score: Int = score
        var level: Array<SkipListNode?> = getLevelArray()

        constructor(value: String, score: Int, level: Array<SkipListNode?>) : this(value, score) {
            this.level = level
        }

        // 申请当前节点的高度
        private fun getRandomLevel(): Int {
            var currentLevel = 1
            while (random.nextInt(2) != 1) {
                currentLevel++
            }
            return if (currentLevel < MAX_LENGTH) currentLevel else MAX_LENGTH
        }

        // 填充当前节点的level数组
        private fun getLevelArray(): Array<SkipListNode?> {
            level = arrayOfNulls(getRandomLevel())
            return level
        }

        override fun toString(): String {
            var levelStr = ""
            level.forEach { levelStr = levelStr + (it?.value ?: "null") + "," }
            return "SkipListNode(value='$value', score=$score, levelLength=${level.size}, level=[$levelStr])"
        }
    }
}

fun main(args:Array<String>){
    val skiplist = SkipList()
    skiplist.insert("hello", 3)
    skiplist.insert("world",3)
    println(skiplist)
}