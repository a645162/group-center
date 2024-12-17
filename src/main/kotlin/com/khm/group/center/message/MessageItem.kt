package com.khm.group.center.message

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.utils.datetime.DateTime

data class MessageItem(
    val content: String,
    val targetUser: String,
    val machineConfig: MachineConfig,
    val sendToPersonBot: Boolean = true,
    val sendToGroupBot: Boolean = true,
    val groupAt: String = "",
) {
    val createTime = DateTime.getCurrentDateTimeStr()

    override fun toString(): String {
        return "[$targetUser]$content"
    }
}
