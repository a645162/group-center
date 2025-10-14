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
     * 测试普通报警消息（默认不紧急）
     */
    @Test
    fun testPushAlarmMessageNormal() {
        val title = "测试普通报警"
        val content = "这是一个普通的测试报警消息，不应该艾特全体成员。"

        // 调用普通报警（默认不紧急）
        botPushService.pushAlarmMessage(title, content, urgent = false)

        // 这里主要是验证方法调用不抛出异常
        // 在实际环境中，会发送到配置的报警群
    }

    /**
     * 测试紧急报警消息（艾特全体成员）
     */
    @Test
    fun testPushAlarmMessageUrgent() {
        val title = "测试紧急报警"
        val content = "这是一个紧急的测试报警消息，应该艾特全体成员。"

        // 调用紧急报警
        botPushService.pushAlarmMessage(title, content, urgent = true)

        // 这里主要是验证方法调用不抛出异常
        // 在实际环境中，会发送到配置的报警群并艾特全体成员
    }

    /**
     * 测试时间同步报警（默认不紧急）
     */
    @Test
    fun testPushTimeSyncAlarmNormal() {
        val machineName = "test-machine"
        val clientTimestamp = DateTimeUtils.getCurrentTimestamp() - 180 // 客户端时间比服务器晚3分钟
        val serverTimestamp = DateTimeUtils.getCurrentTimestamp()
        val timeDiff = 180L // 3分钟
        val threshold = 120L // 2分钟

        // 调用时间同步报警（默认不紧急）
        BotPushService.pushTimeSyncAlarm(machineName, clientTimestamp, serverTimestamp, timeDiff, threshold, urgent = false)

        // 这里主要是验证方法调用不抛出异常
    }

    /**
     * 测试时间同步紧急报警（艾特全体成员）
     */
    @Test
    fun testPushTimeSyncAlarmUrgent() {
        val machineName = "test-machine"
        val clientTimestamp = DateTimeUtils.getCurrentTimestamp() - 300 // 客户端时间比服务器晚5分钟
        val serverTimestamp = DateTimeUtils.getCurrentTimestamp()
        val timeDiff = 300L // 5分钟
        val threshold = 120L // 2分钟

        // 调用时间同步紧急报警
        BotPushService.pushTimeSyncAlarm(machineName, clientTimestamp, serverTimestamp, timeDiff, threshold, urgent = true)

        // 这里主要是验证方法调用不抛出异常
    }

    /**
     * 测试推送到指定群组（普通消息）
     */
    @Test
    fun testPushToGroupNormal() {
        val message = "测试推送到指定群组的普通消息"
        val groupType = "alarm"

        // 调用推送到指定群组（默认不紧急）
        BotPushService.pushToGroup(message, groupType, urgent = false)

        // 这里主要是验证方法调用不抛出异常
    }

    /**
     * 测试推送到指定群组（紧急消息）
     */
    @Test
    fun testPushToGroupUrgent() {
        val message = "测试推送到指定群组的紧急消息"
        val groupType = "alarm"

        // 调用推送到指定群组（紧急）
        BotPushService.pushToGroup(message, groupType, urgent = true)

        // 这里主要是验证方法调用不抛出异常
    }

    /**
     * 测试推送到报警群（普通消息）
     */
    @Test
    fun testPushToAlarmGroupNormal() {
        val message = "测试推送到报警群的普通消息"

        // 调用推送到报警群（默认不紧急）
        BotPushService.pushToAlarmGroup(message, urgent = false)

        // 这里主要是验证方法调用不抛出异常
    }

    /**
     * 测试推送到报警群（紧急消息）
     */
    @Test
    fun testPushToAlarmGroupUrgent() {
        val message = "测试推送到报警群的紧急消息"

        // 调用推送到报警群（紧急）
        BotPushService.pushToAlarmGroup(message, urgent = true)

        // 这里主要是验证方法调用不抛出异常
    }

    /**
     * 测试推送到短期群（普通消息）
     */
    @Test
    fun testPushToShortTermGroupNormal() {
        val message = "测试推送到短期群的普通消息"

        // 调用推送到短期群（默认不紧急）
        BotPushService.pushToShortTermGroup(message, urgent = false)

        // 这里主要是验证方法调用不抛出异常
    }

    /**
     * 测试推送到短期群（紧急消息）
     */
    @Test
    fun testPushToShortTermGroupUrgent() {
        val message = "测试推送到短期群的紧急消息"

        // 调用推送到短期群（紧急）
        BotPushService.pushToShortTermGroup(message, urgent = true)

        // 这里主要是验证方法调用不抛出异常
    }

    /**
     * 测试推送到长期群（普通消息）
     */
    @Test
    fun testPushToLongTermGroupNormal() {
        val message = "测试推送到长期群的普通消息"

        // 调用推送到长期群（默认不紧急）
        BotPushService.pushToLongTermGroup(message, urgent = false)

        // 这里主要是验证方法调用不抛出异常
    }

    /**
     * 测试推送到长期群（紧急消息）
     */
    @Test
    fun testPushToLongTermGroupUrgent() {
        val message = "测试推送到长期群的紧急消息"

        // 调用推送到长期群（紧急）
        BotPushService.pushToLongTermGroup(message, urgent = true)

        // 这里主要是验证方法调用不抛出异常
    }

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