package cn.yz.xr.producer.communication

import cn.yz.xr.common.entity.repo.RMessage

/**
 * author:雷克萨
 * 父actor接受子actor的消息类，未完成功能
 */
data class CommonData(val rMessage:RMessage, val data:Any?)