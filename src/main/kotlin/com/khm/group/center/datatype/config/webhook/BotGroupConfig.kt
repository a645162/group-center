package com.khm.group.center.datatype.config.webhook

class BotGroupConfig {
    var name: String = "" // 群名称，如 "报警群"、"日报群"
    var type: String = "" // 群类型：alarm, daily, weekly, monthly, yearly
    var weComGroupBotKey: String = ""
    var larkGroupBotId: String = ""
    var larkGroupBotKey: String = ""
    var enable: Boolean = true

    fun isValid(): Boolean {
        return (weComGroupBotKey.isNotEmpty() || (larkGroupBotId.isNotEmpty() && larkGroupBotKey.isNotEmpty())) && enable
    }

    fun toSummaryString(): String {
        return "BotGroupConfig(name='$name', type='$type', weComGroupBotKey='${if (weComGroupBotKey.isNotEmpty()) "****" else ""}', larkGroupBotId='$larkGroupBotId', larkGroupBotKey='${if (larkGroupBotKey.isNotEmpty()) "****" else ""}', enable=$enable)"
    }
}
