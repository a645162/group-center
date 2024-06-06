package com.khm.group.center.message

import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.message.webhook.lark.LarkBot
import com.khm.group.center.message.webhook.lark.LarkGroupBot
import com.khm.group.center.message.webhook.wecom.WeComGroupBot


class MessageSender(private val messageItem: MessageItem) {

    val userConfig: GroupUserConfig? =
        GroupUserConfig.getUserByName(messageItem.targetUser)

    fun sendMessage() {
        if (!messageItem.machineConfig.haveValidWebHookService()) {
            println("No any valid webhook server.")
            return
        }

        if (messageItem.machineConfig.weComServer.enable) {
            sendByWeWork()
        }
        if (messageItem.machineConfig.larkServer.enable) {
            sendByLark()
        }
    }

    private fun sendByWeWork() {
        val groupKey = messageItem.machineConfig.weComServer.groupBotKey
        val url = WeComGroupBot.getWebhookUrl(groupKey)

        val mentionedIdList = ArrayList<String>()
        val mentionedMobileList = ArrayList<String>()

        if (userConfig != null) {
            val userId = userConfig.weComUser.userId
            if (userId.isNotEmpty())
                mentionedIdList.add(userId)

            val userMobilePhone = userConfig.weComUser.userMobilePhone
            if (userMobilePhone.isNotEmpty())
                mentionedMobileList.add(userMobilePhone)
        }

        WeComGroupBot.directSendTextWithUrl(
            url, messageItem.content,
            mentionedIdList,
            mentionedMobileList
        )
    }

    private fun sendByLark() {
        val groupBotId = messageItem.machineConfig.larkServer.groupBotId
        val groupBotKey = messageItem.machineConfig.larkServer.groupBotKey

        val larkGroupBotObj = LarkGroupBot(groupBotId, groupBotKey)

        var atText = ""
        if (userConfig != null) {
            val userId = userConfig.larkUser.userId
            atText = LarkGroupBot.getAtUserHtml(userId)

            if (LarkBot.isAppIdSecretValid()) {
                val larkBotObj = LarkBot(userConfig.larkUser.userId)
                larkBotObj.sendText(messageItem.content)
            }
        }
        val finalText = atText + messageItem.content

        larkGroupBotObj.sendText(finalText)
    }
}
