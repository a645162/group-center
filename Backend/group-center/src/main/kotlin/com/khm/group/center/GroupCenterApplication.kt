package com.khm.group.center

import com.khm.group.center.config.ConfigEnvironment
import com.khm.group.center.config.GroupUserConfigParser
import com.khm.group.center.config.MachineConfigParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class GroupCenterApplication {

    final var logger: Logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("GroupCenterApplication init")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // 从环境变量中读取一些配置
            ConfigEnvironment.initializeConfigEnvironment()
            GroupUserConfigParser.readUserYamlFile()
            MachineConfigParser.readMachineYamlFile()

            runApplication<GroupCenterApplication>(*args)
        }
    }
}
