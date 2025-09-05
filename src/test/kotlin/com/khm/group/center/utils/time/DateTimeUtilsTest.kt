package com.khm.group.center.utils.time

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 时间戳转换测试类
 * 对应 Scripts/TestTimestampConversion.kt 的功能
 */
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.yml"])
class DateTimeUtilsTest {

    @Test
    fun testForCustomTimeStamp() {
        println("=== 时间戳转换测试 ===")

        // 测试用户提到的时间戳
        val testTimestamp = 1718872494L

        println("测试时间戳: $testTimestamp")
        println("预期时间: 2024-06-20 16:34:54 (UTC+8)")

        // 使用 DateTimeUtils 的转换逻辑
        val dateTime = DateTimeUtils.convertTimestampToDateTime(testTimestamp)
        val formattedTime = DateTimeUtils.formatDateTimeFull(dateTime)

        println("转换结果: $formattedTime")
        println("时区: ${ZoneId.systemDefault()}")

        // 验证转换结果
        assert(formattedTime == "2024-06-20 16:34:54") { "时间戳转换结果应该正确: $formattedTime" }
    }

}