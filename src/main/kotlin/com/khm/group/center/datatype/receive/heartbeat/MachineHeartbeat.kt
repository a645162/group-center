package com.khm.group.center.datatype.receive.heartbeat

/**
 * 机器心跳请求数据
 */
data class MachineHeartbeat(
    var timestamp: Long = 0,  // 客户端时间戳
    var serverNameEng: String = ""  // 服务器英文名称
)