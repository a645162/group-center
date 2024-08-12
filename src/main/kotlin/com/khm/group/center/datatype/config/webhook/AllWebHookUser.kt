package com.khm.group.center.datatype.config.webhook

import com.khm.group.center.datatype.config.feature.SilentModeConfig

class AllWebHookUser {
    val silentMode: SilentModeConfig = SilentModeConfig()

    var weCom = WeComUser()
    var lark = LarkUser()
}
