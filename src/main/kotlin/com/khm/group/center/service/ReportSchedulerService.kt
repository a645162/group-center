package com.khm.group.center.service

import com.khm.group.center.db.analyse.GpuTaskAnalyse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ReportSchedulerService {

    @Autowired
    lateinit var gpuTaskAnalyse: GpuTaskAnalyse

    @Autowired
    lateinit var botPushService: BotPushService

    /**
     * 每天早上8点生成并推送日报
     */
    @Scheduled(cron = "0 0 8 * * ?")
    fun generateAndPushDailyReport() {
        try {
            val report = gpuTaskAnalyse.getDailyReport()
            val content = formatDailyReport(report)

            val yesterday = LocalDateTime.now().minusDays(1)
            val title = "GPU使用日报 - ${yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"

            botPushService.pushDailyReport(title, content)
            println("Daily report pushed successfully")
        } catch (e: Exception) {
            println("Failed to generate daily report: ${e.message}")
        }
    }

    /**
     * 每周一早上9点生成并推送周报
     */
    @Scheduled(cron = "0 0 9 ? * MON")
    fun generateAndPushWeeklyReport() {
        try {
            val report = gpuTaskAnalyse.getWeeklyReport()
            val content = formatWeeklyReport(report)

            val title = "GPU使用周报 - ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"

            botPushService.pushWeeklyReport(title, content)
            println("Weekly report pushed successfully")
        } catch (e: Exception) {
            println("Failed to generate weekly report: ${e.message}")
        }
    }

    /**
     * 每月1号早上10点生成并推送月报
     */
    @Scheduled(cron = "0 0 10 1 * ?")
    fun generateAndPushMonthlyReport() {
        try {
            val report = gpuTaskAnalyse.getMonthlyReport()
            val content = formatMonthlyReport(report)

            val lastMonth = LocalDateTime.now().minusMonths(1)
            val title = "GPU使用月报 - ${lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))}"

            botPushService.pushMonthlyReport(title, content)
            println("Monthly report pushed successfully")
        } catch (e: Exception) {
            println("Failed to generate monthly report: ${e.message}")
        }
    }

    /**
     * 每年1月1号早上11点生成并推送年报
     */
    @Scheduled(cron = "0 0 11 1 1 ?")
    fun generateAndPushYearlyReport() {
        try {
            val report = gpuTaskAnalyse.getYearlyReport()
            val content = formatYearlyReport(report)

            val lastYear = LocalDateTime.now().minusYears(1)
            val title = "GPU使用年报 - ${lastYear.format(DateTimeFormatter.ofPattern("yyyy"))}"

            botPushService.pushYearlyReport(title, content)
            println("Yearly report pushed successfully")
        } catch (e: Exception) {
            println("Failed to generate yearly report: ${e.message}")
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
            println("Statistics cache updated at ${LocalDateTime.now()}")
        } catch (e: Exception) {
            println("Failed to update statistics cache: ${e.message}")
        }
    }

    private fun formatDailyReport(report: Map<String, Any>): String {
        val date = report["date"] as String
        val totalTasks = report["totalTasks"] as Int
        val totalUsers = report["totalUsers"] as Int
        val totalRuntime = report["totalRuntime"] as Int
        val topUsers = report["topUsers"] as List<*>
        val topGpus = report["topGpus"] as List<*>

        val content = StringBuilder()
        content.append("📊 日报统计\n\n")
        content.append("📅 日期: $date\n")
        content.append("🎯 总任务数: $totalTasks\n")
        content.append("👥 活跃用户数: $totalUsers\n")
        content.append("⏱️ 总运行时间: ${formatTime(totalRuntime)}\n\n")

        if (topUsers.isNotEmpty()) {
            content.append("🏆 活跃用户Top5:\n")
            topUsers.take(5).forEachIndexed { index, user ->
                // 这里需要根据实际的数据结构来格式化
                content.append("${index + 1}. 用户: ${user.toString()}\n")
            }
            content.append("\n")
        }

        if (topGpus.isNotEmpty()) {
            content.append("💻 GPU使用Top5:\n")
            topGpus.take(5).forEachIndexed { index, gpu ->
                content.append("${index + 1}. ${gpu.toString()}\n")
            }
        }

        return content.toString()
    }

    private fun formatWeeklyReport(report: Map<String, Any>): String {
        return formatPeriodReport(report, "周报")
    }

    private fun formatMonthlyReport(report: Map<String, Any>): String {
        return formatPeriodReport(report, "月报")
    }

    private fun formatYearlyReport(report: Map<String, Any>): String {
        return formatPeriodReport(report, "年报")
    }

    private fun formatPeriodReport(report: Map<String, Any>, periodName: String): String {
        val totalTasks = report["totalTasks"] as Int
        val totalUsers = report["totalUsers"] as Int
        val totalRuntime = report["totalRuntime"] as Int
        val topUsers = report["topUsers"] as List<*>
        val topGpus = report["topGpus"] as List<*>

        val content = StringBuilder()
        content.append("📊 $periodName统计\n\n")
        content.append("🎯 总任务数: $totalTasks\n")
        content.append("👥 活跃用户数: $totalUsers\n")
        content.append("⏱️ 总运行时间: ${formatTime(totalRuntime)}\n\n")

        if (topUsers.isNotEmpty()) {
            content.append("🏆 活跃用户Top10:\n")
            topUsers.take(10).forEachIndexed { index, user ->
                content.append("${index + 1}. ${user.toString()}\n")
            }
            content.append("\n")
        }

        if (topGpus.isNotEmpty()) {
            content.append("💻 GPU使用Top10:\n")
            topGpus.take(10).forEachIndexed { index, gpu ->
                content.append("${index + 1}. ${gpu.toString()}\n")
            }
        }

        return content.toString()
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return when {
            hours > 0 -> "${hours}小时${minutes}分钟${remainingSeconds}秒"
            minutes > 0 -> "${minutes}分钟${remainingSeconds}秒"
            else -> "${remainingSeconds}秒"
        }
    }
}
