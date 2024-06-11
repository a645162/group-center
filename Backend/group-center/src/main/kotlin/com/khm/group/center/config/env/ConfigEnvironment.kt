package com.khm.group.center.config.env

import com.khm.group.center.config.env.JsonEnvParser.Companion.parseJsonText
import com.khm.group.center.config.env.TomlEnvParser.Companion.parseTomlText
import com.khm.group.center.config.env.YamlEnvParser.Companion.parseYamlText
import java.io.File

import com.khm.group.center.datatype.utils.datetime.DateTime
import com.khm.group.center.utils.file.ProgramFile

class ConfigEnvironment {

    companion object {
        var FILE_ENV_LIST: HashMap<String, String> = HashMap()

        var PASSWORD_JWT: String = ""

        var LARK_BOT_APP_ID: String = ""
        var LARK_BOT_APP_SECRET: String = ""

        var CLIENT_ENV_CONFIG_PATH: String = ""

        // 10min
        var SilentModeWaitTime: Long = 1000 * 60 * 10

        fun getEnvStr(key: String, defaultValue: String = ""): String {
            val uppercaseKey = key.trim().uppercase()

            if (FILE_ENV_LIST.containsKey(uppercaseKey))
                return FILE_ENV_LIST[uppercaseKey] ?: defaultValue

            return System.getenv(uppercaseKey) ?: defaultValue
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
            printFileEnvList()

            initializePasswordJwt()
            initializeLarkBot()

            initializeOther()
        }

        fun readEnvFile(
            fileEnvListPath: String,
            includeClassName: Boolean = true
        ): HashMap<String, String> {
            val finalResult = HashMap<String, String>()

            val fileText =
                ProgramFile
                    .readFileWithEncodingPredict(
                        fileEnvListPath
                    )

            if (fileEnvListPath.endsWith(".json")) {
                val result = parseJsonText(fileText, includeClassName)
                finalResult.putAll(result)
            } else if (fileEnvListPath.endsWith(".toml")) {
                val result = parseTomlText(fileText, includeClassName)
                finalResult.putAll(result)
            } else if (fileEnvListPath.endsWith(".yaml")) {
                val result = parseYamlText(fileText, includeClassName)
                finalResult.putAll(result)
            }

            return finalResult
        }

        private fun initializeFileEnvList() {
            // Read File
            val fileEnvListPath = getEnvStr(
                "FILE_ENV_PATH",
                "./Debug/FileEnvExample.toml"
            )

            val currentDir = System.getProperty("user.dir")
            println("Current Directory:${currentDir}")

            println("File Env Path:${fileEnvListPath}")
            val file = File(fileEnvListPath)
            if (!file.exists()) {
                println("[Env File]Error File Not Exist")
                return
            }

            FILE_ENV_LIST.putAll(
                readEnvFile(fileEnvListPath)
            )
        }

        private fun printFileEnvList() {
            for (envName in FILE_ENV_LIST.keys) {
                println("${envName}=${FILE_ENV_LIST[envName]}")
            }
        }

        private fun initializePasswordJwt() {
            PASSWORD_JWT = getEnvStr("PASSWORD_JWT")
            if (PASSWORD_JWT.trim { it <= ' ' }.isEmpty()) {
                PASSWORD_JWT = DateTime.getCurrentDateTimeStr()
            }
        }

        private fun initializeLarkBot() {
            LARK_BOT_APP_ID = getEnvStr("LARK_BOT_APP_ID")
            LARK_BOT_APP_SECRET = getEnvStr("LARK_BOT_APP_SECRET")
        }

        private fun initializeOther() {
            CLIENT_ENV_CONFIG_PATH = getEnvStr(
                "CLIENT_ENV_CONFIG_PATH",
                "./Config/Client/ClientEnv.toml"
            )
        }
    }

}
