package com.khm.group.center.datatype.config.parser

import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.config.dashboard.DashBoardSiteConfig

class ShowConfig {
    companion object {
        fun showConfig() {
            showGroupUserConfigList()
            showMachineConfig()
            showDashboardSiteConfig()
        }

        private fun showGroupUserConfigList() {
            val userList = GroupUserConfig.userList
            println("Group User Config(${userList.size}):")

            for (userConfig in userList) {
                println("User: ${userConfig.nameEng}")
            }

            println()
        }

        private fun showMachineConfig() {
            val machineList = MachineConfig.machineList
            println("Machine Config(${machineList.size}):")

            for (machine in machineList) {
                println("Machine: ${machine.name}")
                println("  NameEng: ${machine.nameEng}")
                println("  Host: ${machine.host}")
                println("  Position: ${machine.position}")
                println("  isGpu: ${machine.isGpu}")
            }

            println()
        }

        private fun showDashboardSiteConfig() {
            val siteClassList = DashBoardSiteConfig.siteClassList
            println("Dashboard Site Config(${siteClassList.size}):")

            for (siteClass in siteClassList) {
                println("Site Class: ${siteClass.className}")
                println("  ClassIconUrl: ${siteClass.classIconUrl}")

                for (site in siteClass.sites) {
                    println("  Site: ${site.name}")
                    println("    Url: ${site.url}")
                }
            }

            println()
        }
    }
}
