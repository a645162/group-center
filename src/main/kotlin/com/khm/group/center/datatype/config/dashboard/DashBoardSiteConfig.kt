package com.khm.group.center.datatype.config.dashboard

import com.charleskorn.kaml.Yaml
import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.utils.file.ProgramFile
import kotlinx.serialization.Serializable

class DashBoardSiteConfig {

    companion object {
        private const val MIN_VERSION = 200

        var siteClassList: List<DataDashBoardSiteClass> = listOf()

        @Serializable
        data class DataDashBoardSite(
            val name: String,
            val url: String,
            val iconUrl: String,
            val supportQrCode: Boolean,
        )

        @Serializable
        data class DataDashBoardSiteClass(
            val className: String,
            val classIconUrl: String,
            val position: String,
            val index: Int,

            val sites: List<DataDashBoardSite>
        )

        @Serializable
        data class DataDashBoardConfigFile(
            val version: Int,
            val enable: Boolean,

            val siteList: List<DataDashBoardSiteClass>
        )

        fun readDashboardSiteYamlFile() {
            val dirPath = ConfigEnvironment.getEnvPath(
                "CONFIG_DASHBOARD_SITE_DIR_PATH"
            )
            if (dirPath.isNotEmpty()) {
                val classList = parseDashboardSiteYamlInDir(dirPath)
                siteClassList = classList
                return
            }
            val path = ConfigEnvironment.getEnvStr(
                "CONFIG_DASHBOARD_SITE_PATH",
                ""
            )
            if (path.isEmpty()) {
                return
            }
            val yamlText = ProgramFile.readFile(path)
            val classList = parseDashboardSiteYaml(yamlText)
            siteClassList = classList
        }

        fun parseDashboardSiteYaml(yamlText: String): List<DataDashBoardSiteClass> {
            val classList = mutableListOf<DataDashBoardSiteClass>()

            val configFileTemp = Yaml.default.decodeFromString(
                DataDashBoardConfigFile.serializer(),
                yamlText
            )

            if (!configFileTemp.enable || configFileTemp.version < MIN_VERSION) {
                return classList
            }

            classList.clear()
            classList.addAll(configFileTemp.siteList)

            return classList
        }

        fun parseDashboardSiteYamlInDir(dirPath: String): List<DataDashBoardSiteClass> {
            val classList = mutableListOf<DataDashBoardSiteClass>()

            val yamlPathList = ProgramFile.walkFileTreeKtRecursive(dirPath, "yaml")

            for (yamlFilePath in yamlPathList) {
                try {
                    val yamlText = ProgramFile.readFile(yamlFilePath)

                    val classListTemp = parseDashboardSiteYaml(yamlText)

                    classList.addAll(classListTemp)
                } catch (e: Exception) {
                    println("Read $yamlFilePath Error: ${e.message}")
                }
            }

            return classList
        }

    }

}
