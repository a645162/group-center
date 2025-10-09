package com.khm.group.center.service

import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * 报告推送测试
 * 用于测试昨日日报推送功能
 */
@SpringBootTest
class ReportPushTest {

    @Autowired
    private lateinit var reportPushService: ReportPushService

    @Test
    fun testPushYesterdayReport() {
        logger.info("开始测试昨日日报推送...")
        
        try {
            reportPushService.pushYesterdayReport()
            logger.info("昨日日报推送测试成功")
        } catch (e: Exception) {
            logger.error("昨日日报推送测试失败", e)
            throw e
        }
    }

    @Test
    fun testPushTodayReport() {
        logger.info("开始测试今日日报推送...")
        
        try {
            reportPushService.pushTodayReport()
            logger.info("今日日报推送测试成功")
        } catch (e: Exception) {
            logger.error("今日日报推送测试失败", e)
            throw e
        }
    }

    @Test
    fun testPushWeeklyReport() {
        logger.info("开始测试周报推送...")
        
        try {
            reportPushService.pushWeeklyReport()
            logger.info("周报推送测试成功")
        } catch (e: Exception) {
            logger.error("周报推送测试失败", e)
            throw e
        }
    }
}