package com.khm.group.center.config

import com.khm.group.center.config.TomlEnvParser.Companion.parseTomlText
import java.io.File

import com.khm.group.center.utils.datetime.DateTime
import com.khm.group.center.utils.file.ProgramFile

class ConfigEnvironment {

    companion object {
        var FILE_ENV_LIST: HashMap<String, String> = HashMap()

        var PASSWORD_JWT: String = ""

        fun getEnvStr(key: String, defaultValue: String = ""): String {
            if (FILE_ENV_LIST.containsKey(key))
                return FILE_ENV_LIST[key] ?: defaultValue

            return System.getenv(key) ?: defaultValue
        }

        fun getEnvInt(key: String, defaultValue: Int = 0): Int {
            val strValue = getEnvStr(key)

            if (strValue.isEmpty()) return defaultValue

            try {
                return strValue.toInt()
            } catch (e: NumberFormatException) {
                return defaultValue
            }
        }

        fun initializeConfigEnvironment() {
            initializeFileEnvList()
            initializePasswordJwt()
        }

        private fun initializeFileEnvList() {
            // Read File
            val fileEnvListPath = getEnvStr("FILE_ENV_PATH", "./Debug/FileEnvExample.toml")

            val file = File(fileEnvListPath)

            val fileText =
                ProgramFile
                    .readFileWithEncodingPredict(fileEnvListPath)

            if (fileEnvListPath.endsWith(".json")) {

            } else if (fileEnvListPath.endsWith(".toml")) {
                parseTomlText(fileText)
            } else if (fileEnvListPath.endsWith(".yaml")) {

            } else {
                println("[Env File]Error File Format")
            }

        }

        private fun initializePasswordJwt() {
            PASSWORD_JWT = getEnvStr("PASSWORD_JWT")
            if (PASSWORD_JWT.trim { it <= ' ' }.isEmpty()) {
                PASSWORD_JWT = DateTime.getCurrentDateTimeStr()
            }
        }
    }

}