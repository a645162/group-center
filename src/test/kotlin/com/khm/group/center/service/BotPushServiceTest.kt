package com.khm.group.center.service

import com.khm.group.center.utils.time.DateTimeUtils
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

/**
 * BotPushService 测试类
 * 测试紧急报警功能
 */
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.yml"])
class BotPushServiceTest {

    @Autowired
    private lateinit var botPushService: BotPushService

    /**
     * 测试获取Bot群组配置
     */
    @Test
    fun testGetBotGroups() {
        val groups = botPushService.getAllBotGroups()

        // 验证能够获取到群组配置
        assert(groups.isNotEmpty()) { "应该能够获取到Bot群组配置" }

        // 打印群组信息用于调试
        println("获取到 ${groups.size} 个Bot群组配置:")
        groups.forEach { group ->
            println("  - ${group.name} (${group.type}): 飞书=${group.larkGroupBotId.isNotEmpty()}, 企业微信=${group.weComGroupBotKey.isNotEmpty()}")
        }
    }

    /**
     * 测试按类型获取Bot群组
     */
    @Test
    fun testGetBotGroupsByType() {
        val alarmGroups = botPushService.getBotGroupsByType("alarm")
        val shortTermGroups = botPushService.getBotGroupsByType("shortterm")
        val longTermGroups = botPushService.getBotGroupsByType("longterm")

        // 验证能够按类型获取群组
        println("报警群数量: ${alarmGroups.size}")
        println("短期群数量: ${shortTermGroups.size}")
        println("长期群数量: ${longTermGroups.size}")

        // 这里主要是验证方法调用不抛出异常
    }
}