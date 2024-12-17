package com.khm.group.center.controller.api.client.hardware

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.receive.hardware.HardDiskUserUsage
import com.khm.group.center.message.MessageCenter
import com.khm.group.center.message.MessageItem

class DiskWarningUserNotify(
    var hardDiskUserUsage: HardDiskUserUsage,
    var machineConfig: MachineConfig?
) {

    fun generateWarningMessage(): String {
        return ""
    }

    fun sendUserMessage() {
        if (machineConfig == null) {
            return
        }

        // Send Message
        var finalText = generateWarningMessage()

        finalText = finalText.trim()

        val messageItem = MessageItem(
            content = finalText,
            targetUser = hardDiskUserUsage.userName,
            machineConfig = machineConfig!!,
            sendToPersonBot = true,
            sendToGroupBot = false
        )
        MessageCenter.addNewMessage(messageItem)
    }

}
