package com.khm.group.center.task

import com.khm.group.center.datatype.statistics.SleepAnalysis
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 报告格式化工具类
 * 负责将统计报告数据格式化为可读的消息内容
 */
object ReportFormatter {

    /**
     * 格式化报告消息（包含作息时间分析）
     */
    fun formatReport(report: com.khm.group.center.datatype.statistics.Report, sleepAnalysis: SleepAnalysis? = null): String {
        val content = StringBuilder()
        content.append("${report.title} - ${report.getTimeRangeDescription()} 使用情况\n\n")
        content.append("📅 统计时间: ${report.periodStartDate} - ${report.periodEndDate}\n")
        content.append("🎯 总任务数: ${report.totalTasks}\n")
        content.append("👥 活跃用户数: ${report.activeUsers}\n")
        content.append("⏱️ 总运行时间: ${formatTime(report.totalRuntime)}\n\n")

        if (report.topUsers.isNotEmpty()) {
            val userCount = when (report.reportType) {
                com.khm.group.center.datatype.statistics.ReportType.TODAY, com.khm.group.center.datatype.statistics.ReportType.YESTERDAY -> 3
                com.khm.group.center.datatype.statistics.ReportType.WEEKLY -> 5
                com.khm.group.center.datatype.statistics.ReportType.MONTHLY -> 10
                com.khm.group.center.datatype.statistics.ReportType.YEARLY -> 15
                com.khm.group.center.datatype.statistics.ReportType.CUSTOM -> 5
            }
            content.append("🏆 Top用户:\n")
            report.topUsers.take(userCount).forEachIndexed { index, user ->
                content.append("${index + 1}. ${user.userName}: ${formatTime(user.totalRuntime)} (${user.totalTasks} tasks)\n")
            }
            content.append("\n")
        }

        if (report.topGpus.isNotEmpty()) {
            val gpuCount = when (report.reportType) {
                com.khm.group.center.datatype.statistics.ReportType.TODAY, com.khm.group.center.datatype.statistics.ReportType.YESTERDAY -> 3
                com.khm.group.center.datatype.statistics.ReportType.WEEKLY -> 3
                com.khm.group.center.datatype.statistics.ReportType.MONTHLY -> 5
                com.khm.group.center.datatype.statistics.ReportType.YEARLY -> 8
                com.khm.group.center.datatype.statistics.ReportType.CUSTOM -> 3
            }
            content.append("🔧 Top GPU:\n")
            report.topGpus.take(gpuCount).forEachIndexed { index, gpu ->
                content.append("${index + 1}. ${gpu.gpuName}@${gpu.serverName}: ${formatTime(gpu.totalRuntime)}\n")
            }
            content.append("\n")
        }

        if (report.topProjects.isNotEmpty()) {
            val projectCount = when (report.reportType) {
                com.khm.group.center.datatype.statistics.ReportType.TODAY, com.khm.group.center.datatype.statistics.ReportType.YESTERDAY -> 0
                com.khm.group.center.datatype.statistics.ReportType.WEEKLY -> 3
                com.khm.group.center.datatype.statistics.ReportType.MONTHLY -> 5
                com.khm.group.center.datatype.statistics.ReportType.YEARLY -> 10
                com.khm.group.center.datatype.statistics.ReportType.CUSTOM -> 3
            }
            if (projectCount > 0) {
                content.append("📋 Top项目:\n")
                report.topProjects.take(projectCount).forEachIndexed { index, project ->
                    content.append("${index + 1}. ${project.projectName}: ${formatTime(project.totalRuntime)} (${project.totalTasks} tasks)\n")
                }
                content.append("\n")
            }
        }

        // 添加作息时间分析
        if (sleepAnalysis != null) {
            content.append(formatSleepAnalysis(sleepAnalysis))
        }

        return content.toString()
    }

    /**
     * 格式化时间（秒转换为可读格式）
     */
    fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return when {
            hours > 0 -> "${hours}小时${minutes}分钟${remainingSeconds}秒"
            minutes > 0 -> "${minutes}分钟${remainingSeconds}秒"
            else -> "${remainingSeconds}秒"
        }
    }

    /**
     * 生成今日日报标题
     */
    fun generateTodayTitle(): String {
        val today = LocalDateTime.now()
        return "GPU使用日报 - ${today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
    }

    /**
     * 生成昨日日报标题
     */
    fun generateYesterdayTitle(): String {
        val yesterday = LocalDateTime.now().minusDays(1)
        return "GPU使用日报 - ${yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
    }

    /**
     * 生成周报标题
     */
    fun generateWeeklyTitle(): String {
        return "GPU使用周报 - ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
    }

    /**
     * 生成月报标题
     */
    fun generateMonthlyTitle(): String {
        val lastMonth = LocalDateTime.now().minusMonths(1)
        return "GPU使用月报 - ${lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))}"
    }

    /**
     * 格式化作息时间分析结果
     */
    fun formatSleepAnalysis(sleepAnalysis: SleepAnalysis): String {
        val content = StringBuilder()
        content.append("\n\n🌙 作息时间分析:\n")
        content.append("🌃 熬夜任务数: ${sleepAnalysis.totalLateNightTasks}\n")
        content.append("🌅 早起任务数: ${sleepAnalysis.totalEarlyMorningTasks}\n")
        content.append("👥 熬夜用户数: ${sleepAnalysis.totalLateNightUsers}\n")
        content.append("👥 早起用户数: ${sleepAnalysis.totalEarlyMorningUsers}\n")
        
        // 添加熬夜冠军信息
        sleepAnalysis.lateNightChampion?.let { champion ->
            val championTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(champion.taskStartTime),
                java.time.ZoneId.systemDefault()
            )
            content.append("🏆 熬夜冠军: ${champion.taskUser} (${championTime.format(DateTimeFormatter.ofPattern("HH:mm"))})\n")
        }
        
        // 添加早起冠军信息
        sleepAnalysis.earlyMorningChampion?.let { champion ->
            val championTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(champion.taskStartTime),
                java.time.ZoneId.systemDefault()
            )
            content.append("🏆 早起冠军: ${champion.taskUser} (${championTime.format(DateTimeFormatter.ofPattern("HH:mm"))})\n")
        }
        
        content.append("====================\n")
        return content.toString()
    }

    /**
     * 生成年报标题
     */
    fun generateYearlyTitle(): String {
        val lastYear = LocalDateTime.now().minusYears(1)
        return "GPU使用年报 - ${lastYear.format(DateTimeFormatter.ofPattern("yyyy"))}"
    }
}