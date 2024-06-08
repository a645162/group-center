package com.khm.group.center.security.program

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.security.ProgramCrypto
import com.khm.group.center.datatype.utils.datetime.DateTime

class ClientAccessKey(private var accessKey: String = "") {

    private var originalText: String = ""
    private var encryptedString: String = ""

    var nameEng: String = ""
    var ipAddress: String = ""
    private var dateTimeString: String = DateTime.getCurrentDateTimeStr()

    init {
        if (accessKey.isNotEmpty()) {
            parse()
        }
    }

    data class AccessKey(
        val nameEng: String,
        val ipAddress: String,
        val dateTimeString: String
    )

    private fun parse() {
        originalText =
            ProgramCrypto.decryptJWTString(
                accessKey,
                ConfigEnvironment.PASSWORD_JWT
            )?.toString() ?: ""
        try {
            if (originalText.isNotEmpty()) {
                val jsonObject = JSON.parseObject(originalText) as JSONObject
                nameEng = jsonObject.getString("nameEng") ?: ""
                ipAddress = jsonObject.getString("ipAddress") ?: ""
                dateTimeString = jsonObject.getString("dateTimeString") ?: ""
            }
        } catch (e: Exception) {
            originalText = ""
            e.printStackTrace()
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
            ipAddress = this.ipAddress,
            dateTimeString = this.dateTimeString
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
