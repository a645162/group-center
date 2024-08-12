package com.khm.group.center.controller.api.client.user


import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.datatype.config.webhook.AllWebHookUser
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import io.swagger.v3.oas.annotations.Operation

import com.khm.group.center.datatype.receive.MachineUserMessage
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.message.webhook.lark.LarkBot
import jakarta.validation.Valid
import kotlinx.coroutines.*

@RestController
class ClientUserMessageController {

    @Operation(summary = "机器上的用户自定义消息")
    @RequestMapping("/api/client/user/message", method = [RequestMethod.POST])
    fun machineUserMessage(@Valid @RequestBody machineUserMessage: MachineUserMessage): ClientResponse {
        val responseObj = ClientResponse()
        responseObj.result = "error"
        responseObj.isSucceed = false
        responseObj.isAuthenticated = true

        var userObj =
            GroupUserConfig.getUserByNameEng(machineUserMessage.userName)
        if (userObj == null) {
            userObj =
                GroupUserConfig.getUserByName(machineUserMessage.userName)
        }

        if (userObj != null) {
            if (
                sendMachineUserMessage(
                    userWebhookSetting = userObj.webhook,
                    content = machineUserMessage.content
                )
            ) {
                responseObj.result = "success"
                responseObj.isSucceed = true
            }
        } else {
            responseObj.result = "user not found"
        }

        return responseObj
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendMachineUserMessage(
        userWebhookSetting: AllWebHookUser, content: String
    ): Boolean {
        val larkBotObj = LarkBot(userWebhookSetting.lark.userId)

        GlobalScope.launch {
            larkBotObj.sendTextWithSilentMode(
                text = content,
                silentModeConfig = userWebhookSetting.silentMode
            )
        }

        return true
    }

}
