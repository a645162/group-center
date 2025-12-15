package com.khm.group.center.security.program

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
                val objectMapper = ObjectMapper()
                val jsonObject = objectMapper.readValue<Map<String, String>>(originalText)
                nameEng = jsonObject["nameEng"] ?: ""
                ipAddress = jsonObject["ipAddress"] ?: ""
                dateTimeString = jsonObject["dateTimeString"] ?: ""
            }
        } catch (e: Exception) {
            originalText = ""
            e.printStackTrace()
        }
    }

    fun isValid(): Boolean {
        return originalText.isNotEmpty()
    }

    fun generateAccessKey(): String {
        val accessKey = AccessKey(
            nameEng = this.nameEng,
            ipAddress = this.ipAddress,
            dateTimeString = this.dateTimeString
        )
        val objectMapper = ObjectMapper()
        originalText = objectMapper.writeValueAsString(accessKey)

        encryptedString =
            ProgramCrypto.encryptJWTString(
                originalText,
                ConfigEnvironment.PASSWORD_JWT
            )

        return encryptedString
    }
}
