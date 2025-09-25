package com.khm.group.center.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.service.BaseStatisticsService
import com.khm.group.center.service.CachedStatisticsService
import com.khm.group.center.service.GroupPusher
import com.khm.group.center.utils.time.DateTimeUtils
import com.khm.group.center.utils.time.TimePeriod
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
    lateinit var statisticsService: CachedStatisticsService

    @Autowired
    lateinit var baseStatisticsService: BaseStatisticsService

    @Autowired
    lateinit var groupPusher: GroupPusher

    private val reportStatusDir: Path = Paths.get("Config/Program/Report")

    /**
     * 推送日报到指定群组（昨天的完整日报，昨天凌晨12点到今天凌晨12点）
     */
    fun pushDailyReport(date: LocalDate = LocalDate.now().minusDays(1)) {
        if (!ConfigEnvironment.REPORT_DAILY_ENABLE) {
            println("日报推送已禁用，跳过推送")
            return
        }
        
        val report = statisticsService.getDailyReport(date)
        val sleepAnalysis = getSleepAnalysisForPeriod(TimePeriod.ONE_DAY)
        val message = generateReportString(report, "daily", sleepAnalysis)

        // 推送到短期群（日报）
        GroupPusher.pushToShortTermGroup(message)

        // 记录推送状态
        recordPushStatus("daily", date)
    }

    /**
     * 推送周报到指定群组
     */
    fun pushWeeklyReport() {
        if (!ConfigEnvironment.REPORT_WEEKLY_ENABLE) {
            println("周报推送已禁用，跳过推送")
            return
        }
        
        val report = statisticsService.getWeeklyReport()
        val sleepAnalysis = getSleepAnalysisForPeriod(TimePeriod.ONE_WEEK)
        val message = generateReportString(report, "weekly", sleepAnalysis)

        // 推送到短期群（周报）
        GroupPusher.pushToShortTermGroup(message)

        // 记录推送状态
        recordPushStatus("weekly", LocalDate.now())
    }

    /**
     * 推送月报到指定群组
     */
    fun pushMonthlyReport() {
        if (!ConfigEnvironment.REPORT_MONTHLY_ENABLE) {
            println("月报推送已禁用，跳过推送")
            return
        }
        
        val report = statisticsService.getMonthlyReport()
        val sleepAnalysis = getSleepAnalysisForPeriod(TimePeriod.ONE_MONTH)
        val message = generateReportString(report, "monthly", sleepAnalysis)

        // 推送到长期群（月报）
        GroupPusher.pushToLongTermGroup(message)

        // 记录推送状态
        recordPushStatus("monthly", LocalDate.now())
    }

    /**
     * 推送年报到指定群组
     */
    fun pushYearlyReport() {
        if (!ConfigEnvironment.REPORT_YEARLY_ENABLE) {
            println("年报推送已禁用，跳过推送")
            return
        }
        
        val report = statisticsService.getYearlyReport()
        val sleepAnalysis = getSleepAnalysisForPeriod(TimePeriod.ONE_YEAR)
        val message = generateReportString(report, "yearly", sleepAnalysis)

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

        // 检查日报（检查昨天的日报是否已推送）
        val yesterday = today.minusDays(1)
        if (!isReportPushed("daily", yesterday)) {
            pushDailyReport(yesterday)
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
     * 报告配置数据类
     */
    data class ReportConfig(
        val title: String,
        val timeRange: String,
        val userCount: Int,
        val gpuCount: Int,
        val projectCount: Int
    )

    /**
     * 统一生成报告字符串
     * @param report 报告数据
     * @param reportType 报告类型（daily, weekly, monthly, yearly）
     * @param sleepAnalysis 作息分析数据（可选）
     * @return 格式化后的报告字符串
     */
    private fun generateReportString(report: Any, reportType: String, sleepAnalysis: com.khm.group.center.datatype.statistics.SleepAnalysis? = null): String {
        val config = when (reportType) {
            "daily" -> ReportConfig("📊 GPU使用日报", "最近24小时", 3, 3, 0)
            "weekly" -> ReportConfig("📈 GPU使用周报", "上周", 5, 3, 3)
            "monthly" -> ReportConfig("📈 GPU使用月报", "上月", 10, 5, 5)
            "yearly" -> ReportConfig("🎯 GPU使用年报", "去年", 15, 8, 10)
            else -> ReportConfig("📊 GPU使用报告", "统计期间", 3, 3, 3)
        }
        val baseContent = when (report) {
            is com.khm.group.center.datatype.statistics.DailyReport -> {
                """
                ${config.title} - ${report.date} 使用情况
                ====================
                统计时间: ${formatDateTime(report.startTime)} - ${formatDateTime(report.endTime)}
                总任务数: ${report.totalTasks}
                总运行时间: ${formatTime(report.totalRuntime)}
                活跃用户: ${report.activeUsers}
                任务成功率: ${"%.1f".format(report.successRate)}%
                
                🏆 Top用户:
                ${formatTopUsers(report.topUsers.take(config.userCount))}
                
                🔧 Top GPU:
                ${formatTopGpus(report.topGpus.take(config.gpuCount))}
                """.trimIndent() + if (config.projectCount > 0 && report.topProjects.isNotEmpty()) {
                    "\n\n📋 Top项目:\n${formatTopProjects(report.topProjects.take(config.projectCount))}"
                } else ""
            }
            is com.khm.group.center.datatype.statistics.WeeklyReport -> {
                """
                ${config.title} - ${config.timeRange} 使用情况
                ====================
                统计时间: ${report.periodStartDate} - ${report.periodEndDate}
                总任务数: ${report.totalTasks}
                总运行时间: ${formatTime(report.totalRuntime)}
                活跃用户: ${report.activeUsers}
                
                🏆 ${config.timeRange}Top用户:
                ${formatTopUsers(report.topUsers.take(config.userCount))}
                
                🔧 ${config.timeRange}Top GPU:
                ${formatTopGpus(report.topGpus.take(config.gpuCount))}
                
                📋 ${config.timeRange}Top项目:
                ${formatTopProjects(report.topProjects.take(config.projectCount))}
                """.trimIndent()
            }
            is com.khm.group.center.datatype.statistics.MonthlyReport -> {
                """
                ${config.title} - ${config.timeRange} 使用情况
                ====================
                统计时间: ${report.periodStartDate} - ${report.periodEndDate}
                总任务数: ${report.totalTasks}
                总运行时间: ${formatTime(report.totalRuntime)}
                活跃用户: ${report.activeUsers}
                
                🏆 ${config.timeRange}Top用户:
                ${formatTopUsers(report.topUsers.take(config.userCount))}
                
                🔧 ${config.timeRange}Top GPU:
                ${formatTopGpus(report.topGpus.take(config.gpuCount))}
                
                📋 ${config.timeRange}Top项目:
                ${formatTopProjects(report.topProjects.take(config.projectCount))}
                """.trimIndent()
            }
            is com.khm.group.center.datatype.statistics.YearlyReport -> {
                """
                ${config.title} - ${config.timeRange} 使用情况
                ====================
                统计时间: ${report.periodStartDate} - ${report.periodEndDate}
                总任务数: ${report.totalTasks}
                总运行时间: ${formatTime(report.totalRuntime)}
                活跃用户: ${report.activeUsers}
                
                🏆 ${config.timeRange}Top用户:
                ${formatTopUsers(report.topUsers.take(config.userCount))}
                
                🔧 ${config.timeRange}Top GPU:
                ${formatTopGpus(report.topGpus.take(config.gpuCount))}
                
                📋 ${config.timeRange}Top项目:
                ${formatTopProjects(report.topProjects.take(config.projectCount))}
                """.trimIndent()
            }
            is Map<*, *> -> {
                // 兼容旧的Map格式
                val periodText = when (reportType) {
                    "daily" -> "${LocalDate.now().minusDays(1)}"
                    "weekly" -> "上周"
                    "monthly" -> "上月"
                    "yearly" -> "去年"
                    else -> "统计期间"
                }
                
                """
                ${config.title} - $periodText 使用情况
                ====================
                总任务数: ${report["totalTasks"]}
                总运行时间: ${formatTime((report["totalRuntime"] as Int))}
                活跃用户: ${report["activeUsers"]}
                
                🏆 Top用户:
                ${formatTopUsers((report["topUsers"] as List<*>).take(config.userCount))}
                
                🔧 Top GPU:
                ${formatTopGpus((report["topGpus"] as List<*>).take(config.gpuCount))}
                """.trimIndent() + if (config.projectCount > 0 && report.containsKey("topProjects")) {
                    "\n\n📋 Top项目:\n${formatTopProjects((report["topProjects"] as List<*>).take(config.projectCount))}"
                } else ""
            }
            else -> "❌ 未知的报告格式"
        }
        
        return baseContent + formatSleepAnalysis(sleepAnalysis)
    }
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
     * 格式化Top项目列表
     */
    private fun formatTopProjects(projects: List<*>): String {
        return projects.joinToString("\n") { project ->
            val p = project as com.khm.group.center.datatype.statistics.ProjectStatistics
            "• ${p.projectName}: ${formatTime(p.totalRuntime)} (${p.totalTasks} tasks)"
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
     * 格式化日期时间（精确到分钟）
     * 使用统一的工具类进行格式化
     */
    private fun formatDateTime(dateTime: java.time.LocalDateTime): String {
        return DateTimeUtils.formatDateTimeShort(dateTime)
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
     * 获取指定时间段的作息时间分析
     */
    private fun getSleepAnalysisForPeriod(period: TimePeriod): com.khm.group.center.datatype.statistics.SleepAnalysis? {
        try {
            // 使用基础服务（无缓存）进行作息分析
            val tasks = (baseStatisticsService as com.khm.group.center.service.StatisticsServiceImpl)
                .getTasksByTimePeriod(period)
            
            // 计算时间段的开始和结束时间
            val currentTime = System.currentTimeMillis() / 1000
            val startTime = period.getAgoTimestamp(null) / 1000
            
            return baseStatisticsService.getSleepAnalysis(tasks, startTime, currentTime)
        } catch (e: Exception) {
            println("获取作息时间分析失败: ${e.message}")
            return null
        }
    }

    /**
     * 格式化作息时间分析结果
     */
    private fun formatSleepAnalysis(sleepAnalysis: com.khm.group.center.datatype.statistics.SleepAnalysis?): String {
        if (sleepAnalysis == null) {
            return "❌ 作息分析数据获取失败"
        }
        
        val content = StringBuilder()
        content.append("\n🌙 作息时间分析:\n")
        content.append("🌃 熬夜任务数: ${sleepAnalysis.totalLateNightTasks}\n")
        content.append("🌅 早起任务数: ${sleepAnalysis.totalEarlyMorningTasks}\n")
        content.append("👥 熬夜用户数: ${sleepAnalysis.totalLateNightUsers}\n")
        content.append("👥 早起用户数: ${sleepAnalysis.totalEarlyMorningUsers}\n")
        
        // 添加熬夜冠军信息
        sleepAnalysis.lateNightChampion?.let { champion ->
            val championTime = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(champion.taskStartTime),
                java.time.ZoneId.systemDefault()
            )
            content.append("🏆 熬夜冠军: ${champion.taskUser} (${championTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))})\n")
        }
        
        // 添加早起冠军信息
        sleepAnalysis.earlyMorningChampion?.let { champion ->
            val championTime = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(champion.taskStartTime),
                java.time.ZoneId.systemDefault()
            )
            content.append("🏆 早起冠军: ${champion.taskUser} (${championTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))})\n")
        }
        
        content.append("====================\n")
        return content.toString()
    }

    /**
     * 检查报告是否已推送
     */
    private fun isReportPushed(reportType: String, date: LocalDate): Boolean {
        val statusFile = reportStatusDir.resolve("${reportType}_${date.format(DateTimeFormatter.ISO_DATE)}.toml")
        return Files.exists(statusFile)
    }

}
