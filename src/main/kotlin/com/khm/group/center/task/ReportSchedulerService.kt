package com.khm.group.center.task

import com.khm.group.center.db.analyse.GpuTaskAnalyse
import com.khm.group.center.service.BotPushService
import com.khm.group.center.service.ReportPushService
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Slf4jKt
@Service
class ReportSchedulerService(
    private val gpuTaskAnalyse: GpuTaskAnalyse,
    private val botPushService: BotPushService,
    private val reportPushService: ReportPushService
) {

    /**
     * 每天早上8点生成并推送昨日日报（昨天0:00到今天0:00）
     */
    @Scheduled(cron = "0 0 8 * * ?")
    fun generateAndPushYesterdayReport() {
        try {
            reportPushService.pushYesterdayReport()
            logger.info("✅ Yesterday report pushed successfully")
        } catch (e: Exception) {
            logger.error("❌ Failed to generate yesterday report: ${e.message}", e)
        }
    }

    /**
     * 每天下午4点生成并推送今日日报（今天0:00到明天0:00）
     */
    @Scheduled(cron = "0 0 16 * * ?")
    fun generateAndPushTodayReport() {
        try {
            reportPushService.pushTodayReport()
            logger.info("✅ Today report pushed successfully")
        } catch (e: Exception) {
            logger.error("❌ Failed to generate today report: ${e.message}", e)
        }
    }

    /**
     * 每周一早上9点生成并推送周报
     */
    @Scheduled(cron = "0 0 9 ? * MON")
    fun generateAndPushWeeklyReport() {
        try {
            reportPushService.pushWeeklyReport()
            logger.info("✅ Weekly report pushed successfully")
        } catch (e: Exception) {
            logger.error("❌ Failed to generate weekly report: ${e.message}", e)
        }
    }

    /**
     * 每月1号早上10点生成并推送月报
     */
    @Scheduled(cron = "0 0 10 1 * ?")
    fun generateAndPushMonthlyReport() {
        try {
            reportPushService.pushMonthlyReport()
            logger.info("✅ Monthly report pushed successfully")
        } catch (e: Exception) {
            logger.error("❌ Failed to generate monthly report: ${e.message}", e)
        }
    }

    /**
     * 每年1月1号早上11点生成并推送年报
     */
    @Scheduled(cron = "0 0 11 1 1 ?")
    fun generateAndPushYearlyReport() {
        try {
            reportPushService.pushYearlyReport()
            logger.info("✅ Yearly report pushed successfully")
        } catch (e: Exception) {
            logger.error("❌ Failed to generate yearly report: ${e.message}", e)
        }
    }

    /**
     * 每小时更新统计缓存
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    fun updateStatisticsCache() {
        try {
            // 清除缓存，强制重新计算
            gpuTaskAnalyse.clearCache()
            logger.info("Statistics cache updated at ${LocalDateTime.now()}")
        } catch (e: Exception) {
            logger.error("Failed to update statistics cache: ${e.message}", e)
        }
    }

    /**
     * 每天凌晨5点检查并补推缺失的报告
     */
    @Scheduled(cron = "0 0 5 * * ?")
    fun checkAndPushMissingReports() {
        reportPushService.checkAndPushMissingReports()
    }

    /**
     * 立即执行今日日报推送（用于调试）
     */
    fun pushTodayReportNow() {
        reportPushService.pushTodayReport()
    }

    /**
     * 立即执行昨日日报推送（用于调试）
     */
    fun pushYesterdayReportNow() {
        reportPushService.pushYesterdayReport()
    }

    /**
     * 立即执行周报推送（用于调试）
     */
    fun pushWeeklyReportNow() {
        reportPushService.pushWeeklyReport()
    }

    /**
     * 立即执行月报推送（用于调试）
     */
    fun pushMonthlyReportNow() {
        reportPushService.pushMonthlyReport()
    }

    /**
     * 立即执行年报推送（用于调试）
     */
    fun pushYearlyReportNow() {
        reportPushService.pushYearlyReport()
    }
}