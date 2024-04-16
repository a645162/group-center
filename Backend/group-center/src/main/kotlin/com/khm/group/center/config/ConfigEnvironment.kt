package com.khm.group.center.config

import com.khm.group.center.utils.datetime.DateTime

class ConfigEnvironment {

    companion object {
        var PASSWORD_JWT: String = ""

        fun initializeConfigEnvironment() {
            PASSWORD_JWT = System.getenv("PASSWORD_JWT") ?: ""
            if (PASSWORD_JWT.trim { it <= ' ' }.isEmpty()) {
                PASSWORD_JWT = DateTime.getCurrentDateTimeStr()
            }
        }
    }

}