package com.khm.group.center.message

import com.khm.group.center.datatype.config.MachineConfig
import org.junit.jupiter.api.Test

class MessageCenterTest {

    @Test
    fun testMessageCenter() {
        val messageCenter = MessageCenter()
        messageCenter.startMessageMonitoring()

        val machineConfig = MachineConfig()
        machineConfig.nameEng = "3090"
        machineConfig.webhook.weComServer.enable = true
        machineConfig.webhook.weComServer.groupBotKey = ""

        val messageItem = MessageItem(
            content = "Test Content",
            targetUser = "",
            machineConfig = machineConfig
        )
        messageCenter.enqueueMessage(messageItem)

        Thread.sleep(10000)

        messageCenter.stopMonitoring()
    }

}
