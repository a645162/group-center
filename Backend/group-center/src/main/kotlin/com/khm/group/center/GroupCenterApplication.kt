package com.khm.group.center

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.config.feature.GroupUserConfigParser
import com.khm.group.center.config.feature.MachineConfigParser
import com.khm.group.center.config.feature.ShowConfig
import com.khm.group.center.message.MessageCenter
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@Slf4jKt
@SpringBootApplication
@MapperScan("com.khm.group.center.db.mapper")
@ComponentScan("com.khm.group.center.*")
class GroupCenterApplication {

    // final var logger: Logger = LoggerFactory.getLogger(javaClass)

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
            ShowConfig.showConfig()

            // 初始化 消息中心
            MessageCenter.startMessageCenter()

            runApplication<GroupCenterApplication>(*args)
        }
    }
}
