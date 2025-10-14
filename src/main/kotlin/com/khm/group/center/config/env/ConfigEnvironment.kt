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

        var MACHINE_AUTH_REMEMBER_IP: Boolean = true
        var RUN_IN_DOCKER: Boolean = false

        var GROUP_BOT_AT_ENABLE: Boolean = false

        var LARK_BOT_APP_ID: String = ""
        var LARK_BOT_APP_SECRET: String = ""

        var CLIENT_ENV_CONFIG_PATH: String = ""

        var USER_FILE_SAVE_PATH: String = ""

        // 10min
        var SilentModeWaitTime: Long = 1000 * 60 * 5

        // 多卡过滤开关
        var FILTER_MULTI_GPU_TASKS: Boolean = true

        // 报告推送开关
        var REPORT_DAILY_ENABLE: Boolean = true
        var REPORT_WEEKLY_ENABLE: Boolean = true
        var REPORT_MONTHLY_ENABLE: Boolean = true
        var REPORT_YEARLY_ENABLE: Boolean = true

        // 报警开关
        var ALARM_PING_FAILURE_ENABLE: Boolean = true
        var ALARM_AGENT_OFFLINE_ENABLE: Boolean = true
        var ALARM_TIME_SYNC_ENABLE: Boolean = true

        fun getEnvStr(key: String, defaultValue: String = ""): String {
            val uppercaseKey = key.trim().uppercase()

            if (FILE_ENV_LIST.containsKey(uppercaseKey))
                return FILE_ENV_LIST[uppercaseKey] ?: defaultValue

            return System.getenv(uppercaseKey) ?: defaultValue
        }

        fun getEnvPath(key: String, defaultValue: String = ""): String {
            return getEnvStr(key, defaultValue)
                .replace("\"", "")
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

        fun getEnvBool(key: String, defaultValue: Boolean = false): Boolean {
            val envString = getEnvStr(key, "")

            if (envString.isEmpty()) return defaultValue

            return try {
                val upper_string = envString.trim().uppercase()

                (upper_string == "TRUE" || upper_string == "1")
            } catch (e: Exception) {
                defaultValue
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

            USER_FILE_SAVE_PATH = getEnvStr(
                "USER_FILE_SAVE_PATH",
                "./Config/UserFiles"
            )

            RUN_IN_DOCKER = getEnvBool(
                "RUN_IN_DOCKER",
                RUN_IN_DOCKER
            )
            if (RUN_IN_DOCKER) {
                println("Run in Docker")
            } else {
                println("Run in Machine")
            }
            MACHINE_AUTH_REMEMBER_IP = getEnvBool(
                "MACHINE_AUTH_REMEMBER_IP",
                MACHINE_AUTH_REMEMBER_IP
            ) && (!RUN_IN_DOCKER)
            if (MACHINE_AUTH_REMEMBER_IP) {
                println("Machine Auth Remember IP is Enabled")
            } else {
                println("Machine Auth Remember IP is Disabled")
            }

            GROUP_BOT_AT_ENABLE = getEnvBool(
                "GROUP_BOT_AT_ENABLE",
                GROUP_BOT_AT_ENABLE
            )

            // 初始化多卡过滤开关
            FILTER_MULTI_GPU_TASKS = getEnvBool(
                "FILTER_MULTI_GPU_TASKS",
                FILTER_MULTI_GPU_TASKS
            )

            // 初始化报告推送开关
            REPORT_DAILY_ENABLE = getEnvBool(
                "REPORT_DAILY_ENABLE",
                REPORT_DAILY_ENABLE
            )
            REPORT_WEEKLY_ENABLE = getEnvBool(
                "REPORT_WEEKLY_ENABLE",
                REPORT_WEEKLY_ENABLE
            )
            REPORT_MONTHLY_ENABLE = getEnvBool(
                "REPORT_MONTHLY_ENABLE",
                REPORT_MONTHLY_ENABLE
            )
            REPORT_YEARLY_ENABLE = getEnvBool(
                "REPORT_YEARLY_ENABLE",
                REPORT_YEARLY_ENABLE
            )

            // 初始化报警开关
            ALARM_PING_FAILURE_ENABLE = getEnvBool(
                "ALARM_PING_FAILURE_ENABLE",
                ALARM_PING_FAILURE_ENABLE
            )
            ALARM_AGENT_OFFLINE_ENABLE = getEnvBool(
                "ALARM_AGENT_OFFLINE_ENABLE",
                ALARM_AGENT_OFFLINE_ENABLE
            )
            ALARM_TIME_SYNC_ENABLE = getEnvBool(
                "ALARM_TIME_SYNC_ENABLE",
                ALARM_TIME_SYNC_ENABLE
            )
        }
    }

}
