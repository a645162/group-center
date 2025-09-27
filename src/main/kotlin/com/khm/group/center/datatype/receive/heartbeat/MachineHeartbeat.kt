package com.khm.group.center.datatype.receive.heartbeat

import kotlinx.serialization.Serializable

/**
 * 机器心跳请求数据
 */
@Serializable
data class MachineHeartbeat(
    val timestamp: Long,  // 客户端时间戳
    val serverNameEng: String  // 服务器英文名称
)