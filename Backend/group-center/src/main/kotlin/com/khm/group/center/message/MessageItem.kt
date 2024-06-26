package com.khm.group.center.message

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.utils.datetime.DateTime

data class MessageItem(
    val content: String,
    val targetUser: String,
    val machineConfig: MachineConfig,
) {
    val createTime = DateTime.getCurrentDateTimeStr()

    override fun toString(): String {
        return "[$targetUser]$content"
    }
}
