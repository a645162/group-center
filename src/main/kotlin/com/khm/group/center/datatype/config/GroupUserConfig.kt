package com.khm.group.center.datatype.config

import com.khm.group.center.datatype.config.info.LinuxUser
import com.khm.group.center.datatype.config.webhook.AllWebHookUser


class GroupUserConfig {
    var name: String = ""
    var nameEng: String = ""
    var keywords: List<String> = listOf()
    var year: Int = 2024

    var linuxUser: LinuxUser = LinuxUser()

    var webhook = AllWebHookUser()

    companion object {
        var userList: List<GroupUserConfig> = listOf()

        fun getUserByName(name: String): GroupUserConfig? {
            val searchString = name.trim()

            for (user in userList) {
                if (user.name == searchString) {
                    return user
                }
            }

            return null
        }

        fun getUserByNameEng(nameEng: String): GroupUserConfig? {
            val searchString = nameEng.trim()

            for (user in userList) {
                if (user.nameEng == searchString) {
                    return user
                }
            }

            return null
        }
    }
}
