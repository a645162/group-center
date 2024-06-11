package com.khm.group.center.message

import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.message.webhook.lark.LarkBot
import com.khm.group.center.message.webhook.lark.LarkGroupBot
import com.khm.group.center.message.webhook.wecom.WeComGroupBot
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class MessageSender(private val messageItem: MessageItem) {

    private val userConfig: GroupUserConfig? =
        GroupUserConfig.getUserByName(messageItem.targetUser)

    fun sendMessage() {
        if (!messageItem.machineConfig.webhook.haveValidWebHookService()) {
            println("No any valid webhook server.")
            return
        }

        if (messageItem.machineConfig.webhook.weComServer.enable) {
            sendByWeWork()
        }
        if (messageItem.machineConfig.webhook.larkServer.enable) {
            runBlocking {
                kotlin.run {
                    sendByLark()
                }
            }
        }
    }

    private fun sendByWeWork() {
        val groupKey =
            messageItem.machineConfig.webhook.weComServer.groupBotKey
        val url = WeComGroupBot.getWebhookUrl(groupKey)

        val mentionedIdList = ArrayList<String>()
        val mentionedMobileList = ArrayList<String>()

        if (userConfig != null) {
            val userId = userConfig.webhook.weCom.userId
            if (userId.isNotEmpty())
                mentionedIdList.add(userId)

            val userMobilePhone = userConfig.webhook.weCom.userMobilePhone
            if (userMobilePhone.isNotEmpty())
                mentionedMobileList.add(userMobilePhone)
        }

        WeComGroupBot.directSendTextWithUrl(
            url, messageItem.content,
            mentionedIdList,
            mentionedMobileList
        )
    }

    private suspend fun sendByLark() = coroutineScope {
        val machineName = messageItem.machineConfig.name
        val machineUrl = "http://" + messageItem.machineConfig.host

        val groupBotId = messageItem.machineConfig.webhook.larkServer.groupBotId
        val groupBotKey = messageItem.machineConfig.webhook.larkServer.groupBotKey

        val larkGroupBotObj = LarkGroupBot(groupBotId, groupBotKey)

        var atText = ""
        if (userConfig != null) {
            val userId = userConfig.webhook.lark.userId
            if (userId.isEmpty()) {
                atText = userConfig.name
            } else {
                atText = LarkGroupBot.getAtUserHtml(userId)

                if (LarkBot.isAppIdSecretValid()) {
                    val larkBotObj = LarkBot(userConfig.webhook.lark.userId)
                    val text = (
                            messageItem.content.trim()
                                    + "\n\n"
                                    + "${machineName}:\n"
                                    + machineUrl
                            )

                    launch {
                        larkBotObj.sendTextWithSilentMode(
                            text, userConfig.webhook.silentMode
                        )
                    }
                }
            }
        }
        val finalText = atText + messageItem.content

        val groupBotText = (
                finalText
                        + "\n\n"
                        + machineUrl
                )

        launch {
            larkGroupBotObj.sendTextWithSilentMode(
                groupBotText, messageItem.machineConfig.webhook.silentMode
            )
        }
    }
}
