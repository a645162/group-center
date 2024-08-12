package com.khm.group.center.datatype.config

import com.khm.group.center.datatype.config.dashboard.DashBoardSiteConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DashBoardSiteConfigTest {
    @Test
    fun testRead() {
        DashBoardSiteConfig.readDashboardSiteYamlFile()

        println("Size: ${DashBoardSiteConfig.siteClassList.size}")

        DashBoardSiteConfig.siteClassList.forEach {
            println(it.className)
            it.sites.forEach {
                println(it.name)
            }
        }
    }
}
