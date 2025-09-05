package com.khm.group.center.task

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 报告格式化工具类
 * 负责将统计报告数据格式化为可读的消息内容
 */
object ReportFormatter {

    /**
     * 格式化日报消息
     */
    fun formatDailyReport(report: Map<String, Any>): String {
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

    /**
     * 格式化周报消息
     */
    fun formatWeeklyReport(report: Map<String, Any>): String {
        return formatPeriodReport(report, "周报")
    }

    /**
     * 格式化月报消息
     */
    fun formatMonthlyReport(report: Map<String, Any>): String {
        return formatPeriodReport(report, "月报")
    }

    /**
     * 格式化年报消息
     */
    fun formatYearlyReport(report: Map<String, Any>): String {
        return formatPeriodReport(report, "年报")
    }

    /**
     * 格式化周期性报告消息
     */
    private fun formatPeriodReport(report: Map<String, Any>, periodName: String): String {
        val totalTasks = report["totalTasks"] as Int
        val totalUsers = report["totalUsers"] as Int
        val totalRuntime = report["totalRuntime"] as Int
        val topUsers = report["topUsers"] as List<*>
        val topGpus = report["topGpus"] as List<*>

        val content = StringBuilder()
        content.append("📊 $periodName 统计\n\n")
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
     * 生成日报标题
     */
    fun generateDailyTitle(): String {
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
     * 生成年报标题
     */
    fun generateYearlyTitle(): String {
        val lastYear = LocalDateTime.now().minusYears(1)
        return "GPU使用年报 - ${lastYear.format(DateTimeFormatter.ofPattern("yyyy"))}"
    }
}