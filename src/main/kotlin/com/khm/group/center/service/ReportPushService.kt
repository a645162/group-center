package com.khm.group.center.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.khm.group.center.service.GroupPusher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class ReportPushService {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var statisticsService: StatisticsService

    @Autowired
    lateinit var groupPusher: GroupPusher

    private val reportStatusDir: Path = Paths.get("Config/Program/Report")

    /**
     * 推送日报到指定群组
     */
    fun pushDailyReport(date: LocalDate = LocalDate.now()) {
        val report = statisticsService.getDailyReport(date)
        val message = formatDailyReport(report)

        // 推送到短期群（日报）
        GroupPusher.pushToShortTermGroup(message)

        // 记录推送状态
        recordPushStatus("daily", date)
    }

    /**
     * 推送周报到指定群组
     */
    fun pushWeeklyReport() {
        val report = statisticsService.getWeeklyReport()
        val message = formatWeeklyReport(report)

        // 推送到短期群（周报）
        GroupPusher.pushToShortTermGroup(message)

        // 记录推送状态
        recordPushStatus("weekly", LocalDate.now())
    }

    /**
     * 推送月报到指定群组
     */
    fun pushMonthlyReport() {
        val report = statisticsService.getMonthlyReport()
        val message = formatMonthlyReport(report)

        // 推送到长期群（月报）
        GroupPusher.pushToLongTermGroup(message)

        // 记录推送状态
        recordPushStatus("monthly", LocalDate.now())
    }

    /**
     * 推送年报到指定群组
     */
    fun pushYearlyReport() {
        val report = statisticsService.getYearlyReport()
        val message = formatYearlyReport(report)

        // 推送到长期群（年报）
        GroupPusher.pushToLongTermGroup(message)

        // 记录推送状态
        recordPushStatus("yearly", LocalDate.now())
    }

    /**
     * 检查并补推缺失的报告
     */
    fun checkAndPushMissingReports() {
        val today = LocalDate.now()

        // 检查日报
        if (!isReportPushed("daily", today)) {
            pushDailyReport(today)
        }

        // 检查周报（每周一检查上周的周报）
        if (today.dayOfWeek.value == 1) { // Monday
            val lastWeek = today.minusWeeks(1)
            if (!isReportPushed("weekly", lastWeek)) {
                pushWeeklyReport()
            }
        }

        // 检查月报（每月1号检查上月的月报）
        if (today.dayOfMonth == 1) {
            val lastMonth = today.minusMonths(1)
            if (!isReportPushed("monthly", lastMonth)) {
                pushMonthlyReport()
            }
        }

        // 检查年报（每年1月1号检查去年的年报）
        if (today.monthValue == 1 && today.dayOfMonth == 1) {
            val lastYear = today.minusYears(1)
            if (!isReportPushed("yearly", lastYear)) {
                pushYearlyReport()
            }
        }
    }

    /**
     * 格式化日报消息
     */
     private fun formatDailyReport(report: Any): String {
         return when (report) {
             is com.khm.group.center.datatype.statistics.DailyReport -> {
                 """
                 📊 GPU使用日报 - ${report.date}
                 ====================
                 总任务数: ${report.totalTasks}
                 总运行时间: ${formatTime(report.totalRuntime)}
                 活跃用户: ${report.activeUsers}
                 任务成功率: ${"%.1f".format(report.successRate)}%
                 
                 🏆 今日Top用户:
                 ${formatTopUsers(report.topUsers.take(3))}
                 
                 🔧 今日Top GPU:
                 ${formatTopGpus(report.topGpus.take(3))}
                 """.trimIndent()
             }
             is Map<*, *> -> {
                 """
                 📊 GPU使用日报 - ${LocalDate.now()}
                 ====================
                 总任务数: ${report["totalTasks"]}
                 总运行时间: ${formatTime((report["totalRuntime"] as Int))}
                 活跃用户: ${report["activeUsers"]}
                 任务成功率: ${"%.1f".format(report["successRate"] as Double)}%
                 
                 🏆 今日Top用户:
                 ${formatTopUsers((report["topUsers"] as List<*>).take(3))}
                 
                 🔧 今日Top GPU:
                 ${formatTopGpus((report["topGpus"] as List<*>).take(3))}
                 """.trimIndent()
             }
             else -> "❌ 未知的报告格式"
         }
     }
    /**
     * 格式化周报消息
     */
    private fun formatWeeklyReport(report: Any): String {
        return when (report) {
            is com.khm.group.center.datatype.statistics.WeeklyReport -> {
                """
                📈 GPU使用周报 (${report.startDate} - ${report.endDate})
                ====================
                总任务数: ${report.totalTasks}
                总运行时间: ${formatTime(report.totalRuntime)}
                活跃用户: ${report.activeUsers}
                
                🏆 本周Top用户:
                ${formatTopUsers(report.topUsers.take(5))}
                """.trimIndent()
            }
            is Map<*, *> -> {
                """
                📈 GPU使用周报
                ====================
                总任务数: ${report["totalTasks"]}
                总运行时间: ${formatTime((report["totalRuntime"] as Int))}
                活跃用户: ${report["activeUsers"]}
                
                🏆 本周Top用户:
                ${formatTopUsers((report["topUsers"] as List<*>).take(5))}
                """.trimIndent()
            }
            else -> "❌ 未知的报告格式"
        }
    }

    /**
     * 格式化月报消息
     */
    private fun formatMonthlyReport(report: Any): String {
        return when (report) {
            is com.khm.group.center.datatype.statistics.MonthlyReport -> {
                """
                📈 GPU使用月报 - ${report.year}年${report.month.value}月
                ====================
                总任务数: ${report.totalTasks}
                总运行时间: ${formatTime(report.totalRuntime)}
                活跃用户: ${report.activeUsers}
                
                🏆 本月Top用户:
                ${formatTopUsers(report.topUsers.take(10))}
                """.trimIndent()
            }
            is Map<*, *> -> {
                """
                📈 GPU使用月报
                ====================
                总任务数: ${report["totalTasks"]}
                总运行时间: ${formatTime((report["totalRuntime"] as Int))}
                活跃用户: ${report["activeUsers"]}
                
                🏆 本月Top用户:
                ${formatTopUsers((report["topUsers"] as List<*>).take(10))}
                """.trimIndent()
            }
            else -> "❌ 未知的报告格式"
        }
    }

    /**
     * 格式化年报消息
     */
    private fun formatYearlyReport(report: Any): String {
        return when (report) {
            is com.khm.group.center.datatype.statistics.YearlyReport -> {
                """
                🎯 GPU使用年报 - ${report.year}年
                ====================
                总任务数: ${report.totalTasks}
                总运行时间: ${formatTime(report.totalRuntime)}
                活跃用户: ${report.activeUsers}
                
                🏆 年度Top用户:
                ${formatTopUsers(report.topUsers.take(15))}
                """.trimIndent()
            }
            is Map<*, *> -> {
                """
                🎯 GPU使用年报
                ====================
                总任务数: ${report["totalTasks"]}
                总运行时间: ${formatTime((report["totalRuntime"] as Int))}
                活跃用户: ${report["activeUsers"]}
                
                🏆 年度Top用户:
                ${formatTopUsers((report["topUsers"] as List<*>).take(15))}
                """.trimIndent()
            }
            else -> "❌ 未知的报告格式"
        }
    }

    /**
     * 格式化Top用户列表
     */
    private fun formatTopUsers(users: List<*>): String {
        return users.joinToString("\n") { user ->
            val u = user as com.khm.group.center.datatype.statistics.UserStatistics
            "• ${u.userName}: ${formatTime(u.totalRuntime)} (${u.totalTasks} tasks)"
        }
    }

    /**
     * 格式化Top GPU列表
     */
    private fun formatTopGpus(gpus: List<*>): String {
        return gpus.joinToString("\n") { gpu ->
            val g = gpu as com.khm.group.center.datatype.statistics.GpuStatistics
            "• ${g.gpuName}@${g.serverName}: ${formatTime(g.totalRuntime)}"
        }
    }

    /**
     * 格式化时间（秒转换为可读格式）
     */
    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return "${hours}h${minutes}m"
    }

    /**
     * 记录推送状态
     */
    private fun recordPushStatus(reportType: String, date: LocalDate) {
        val statusFile = reportStatusDir.resolve("${reportType}_${date.format(DateTimeFormatter.ISO_DATE)}.toml")

        val status = mapOf(
            "report_type" to reportType,
            "push_date" to LocalDate.now().format(DateTimeFormatter.ISO_DATE),
            "push_time" to System.currentTimeMillis(),
            "status" to "success"
        )

        Files.createDirectories(reportStatusDir)
        Files.writeString(statusFile, objectMapper.writeValueAsString(status))
    }

    /**
     * 检查报告是否已推送
     */
    private fun isReportPushed(reportType: String, date: LocalDate): Boolean {
        val statusFile = reportStatusDir.resolve("${reportType}_${date.format(DateTimeFormatter.ISO_DATE)}.toml")
        return Files.exists(statusFile)
    }

}
