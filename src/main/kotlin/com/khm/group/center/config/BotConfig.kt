package com.khm.group.center.config

// Bot配置数据结构
data class BotConfig(
    val bot: BotGroups = BotGroups()
)

data class BotGroups(
    val groups: List<BotGroup> = emptyList()
)

data class BotGroup(
    val name: String = "",
    val type: String = "",
    val weComGroupBotKey: String = "",
    val larkGroupBotId: String = "",
    val larkGroupBotKey: String = "",
    val enable: Boolean = true
)