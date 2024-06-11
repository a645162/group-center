package com.khm.group.center.datatype.config.webhook

import com.khm.group.center.datatype.config.feature.SilentModeConfig

class AllWebHookServer {
    val silentMode: SilentModeConfig = SilentModeConfig()

    val weComServer = WeComServer()
    val larkServer = LarkServer()

    private var webhookServerList: List<BaseWebHookServer> = listOf(
        weComServer,
        larkServer
    )

    fun haveValidWebHookService(): Boolean {
        for (webhookServer in webhookServerList) {
            if (webhookServer.enable) {
                return true
            }
        }

        return false
    }
}