package com.khm.group.center.message

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.message.webhook.lark.LarkBot
import com.khm.group.center.message.webhook.lark.LarkGroupBot
import com.khm.group.center.message.webhook.wecom.WeComGroupBot
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MessageSender(private val messageItem: MessageItem) {

    // Get User Config Object by User Name
    private val userConfig: GroupUserConfig? =
        GroupUserConfig.getUserByName(messageItem.targetUser)

    suspend fun sendMessage() = coroutineScope {
        if (!messageItem.machineConfig.webhook.haveValidWebHookService()) {
            println("No any valid webhook server.")
            return@coroutineScope
        }

        if (messageItem.machineConfig.webhook.weComServer.enable) {
            launch {
                sendByWeWork()
            }
        }
        if (messageItem.machineConfig.webhook.larkServer.enable) {
            launch {
                sendByLark()
            }
        }
    }

    private suspend fun sendByWeWork() = coroutineScope {
        if (!messageItem.sendToGroupBot) {
            return@coroutineScope
        }

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

        println("Try to async send WeCom text with silent mode for ${userConfig?.nameEng}")
        while (messageItem.machineConfig.webhook.silentMode.isSilentMode()) {
            // Delay
            delay(ConfigEnvironment.SilentModeWaitTime)
        }

        WeComGroupBot.directSendTextWithUrl(
            url, messageItem.content,
            mentionedIdList,
            mentionedMobileList
        )
        println("Sent WeCom text with silent mode for ${userConfig?.nameEng}")
    }

    private suspend fun sendByLark() = coroutineScope {
        val machineName = messageItem.machineConfig.name
        val machineUrl = "http://" + messageItem.machineConfig.host

        val groupBotId = messageItem.machineConfig.webhook.larkServer.groupBotId
        val groupBotKey = messageItem.machineConfig.webhook.larkServer.groupBotKey

        val larkGroupBotObj = LarkGroupBot(groupBotId, groupBotKey)

        var atText = ""
        if (messageItem.sendToPersonBot && userConfig != null) {
            // User Personal Bot
            val userId = userConfig.webhook.lark.userId

            if (!userId.isEmpty()) {
                if (ConfigEnvironment.GROUP_BOT_AT_ENABLE)
                    atText = LarkGroupBot.getAtUserHtml(userId)

                if (LarkBot.isAppIdSecretValid()) {
                    val larkBotObj = LarkBot(userConfig.webhook.lark.userId)
                    val text = (
                            "[${machineName}]"
                                    + messageItem.content.trim()
                                    + "\n"
                                    + machineUrl
                            )

                    // Send Personal Bot
                    launch {
                        larkBotObj.sendTextWithSilentMode(
                            text, userConfig.webhook.silentMode
                        )
                    }
                }
            }

        }

        atText = atText.trim()

        if (messageItem.groupAt.isNotEmpty()) {
            val atName = messageItem.groupAt.trim()

            if (atName.uppercase() == "ALL") {
                atText = LarkGroupBot.getAtUserHtml("all")
            } else {
                val atUserConfig = GroupUserConfig.getUserByName(atName)
                if (atUserConfig != null) {
                    atText += LarkGroupBot.getAtUserHtml(atUserConfig.webhook.lark.userId)
                }
            }
        }

        if (userConfig != null && atText.isBlank()) {
            atText = userConfig.name
        }

        atText = atText.trim()

        val finalText = atText + messageItem.content

        // Lark Group Bot
        val groupBotText = finalText

        if (messageItem.sendToGroupBot) {
            launch {
                larkGroupBotObj.sendTextWithSilentMode(
                    groupBotText, messageItem.machineConfig.webhook.silentMode
                )
            }
        }
    }
}
