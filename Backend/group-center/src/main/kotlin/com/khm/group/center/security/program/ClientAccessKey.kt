package com.khm.group.center.security.program

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.khm.group.center.config.ConfigEnvironment
import com.khm.group.center.security.ProgramCrypto

class ClientAccessKey(private var accessKey: String = "") {

    private var originalText: String = ""
    private var encryptedString: String = ""

    var nameEng: String = ""
    var ip: String = ""
    var time: String = ""

    init {
        if (accessKey.isNotEmpty()) {
            parse()
        }
    }

    data class AccessKey(
        val nameEng: String,
        val ip: String,
        val time: String
    )

    private fun parse() {
        originalText =
            ProgramCrypto.decryptJWTString(
                accessKey,
                ConfigEnvironment.PASSWORD_JWT
            )?.toString() ?: ""
        if (originalText.isNotEmpty()) {
            val jsonObject = JSON.parseObject(originalText) as JSONObject
            nameEng = jsonObject.getString("nameEng") ?: ""
            ip = jsonObject.getString("ip") ?: ""
            time = jsonObject.getString("time") ?: ""
        }
    }

    fun isValid(): Boolean {
        if (originalText.isEmpty()) {
            return false
        }

        return true
    }

    fun generateAccessKey(): String {
        val accessKey = AccessKey(
            nameEng = this.nameEng,
            ip = this.ip,
            time = this.time
        )
        originalText = JSON.toJSONString(accessKey)

        encryptedString =
            ProgramCrypto.encryptJWTString(
                originalText,
                ConfigEnvironment.PASSWORD_JWT
            )

        return encryptedString
    }
}
