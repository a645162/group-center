package com.khm.group.center.datatype.config

import com.khm.group.center.datatype.config.webhook.WeComUser
import com.khm.group.center.datatype.config.webhook.LarkUser


class GroupUserConfig {
    var name: String = ""
    var nameEng: String = ""
    var keywords: List<String> = listOf()
    var year: Int = 2024

    var weComUser = WeComUser()
    var larkUser = LarkUser()

    companion object {
        var userList: List<GroupUserConfig> = listOf()

        fun getUserByName(name: String): GroupUserConfig? {
            for (user in userList) {
                if (user.name == name) {
                    return user
                }
            }

            return null
        }
    }
}
