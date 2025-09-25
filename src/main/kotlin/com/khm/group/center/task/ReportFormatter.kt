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
     * 格式化日报消息（包含作息时间分析）
     */
      fun formatDailyReport(report: Map<String, Any>, sleepAnalysis: SleepAnalysis? = null): String {
          val date = report["date"] as String
          val totalTasks = report["totalTasks"] as Int
          val totalUsers = report["totalUsers"] as Int
          val totalRuntime = report["totalRuntime"] as Int
          val topUsers = report["topUsers"] as List<*>
          val topGpus = report["topGpus"] as List<*>
          val topProjects = report["topProjects"] as List<*>?
 
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
              content.append("\n")
          }
 
          if (topProjects != null && topProjects.isNotEmpty()) {
              content.append("📋 项目使用Top5:\n")
              topProjects.take(5).forEachIndexed { index, project ->
                  content.append("${index + 1}. ${project.toString()}\n")
              }
          }
 
          // 添加作息时间分析
          if (sleepAnalysis != null) {
              content.append(formatSleepAnalysis(sleepAnalysis))
          }
 
        return content.toString()
    }
   /**
    * 格式化周报消息（包含作息时间分析）
    */
   fun formatWeeklyReport(report: Map<String, Any>, sleepAnalysis: SleepAnalysis? = null): String {
       return formatPeriodReport(report, "周报", sleepAnalysis)
   }

   /**
    * 格式化月报消息（包含作息时间分析）
    */
   fun formatMonthlyReport(report: Map<String, Any>, sleepAnalysis: SleepAnalysis? = null): String {
       return formatPeriodReport(report, "月报", sleepAnalysis)
   }

   /**
    * 格式化年报消息（包含作息时间分析）
    */
   fun formatYearlyReport(report: Map<String, Any>, sleepAnalysis: SleepAnalysis? = null): String {
       return formatPeriodReport(report, "年报", sleepAnalysis)
   }

    /**
     * 格式化周期性报告消息（包含作息时间分析）
     */
    private fun formatPeriodReport(report: Map<String, Any>, periodName: String, sleepAnalysis: SleepAnalysis? = null): String {
        val totalTasks = report["totalTasks"] as Int
        val totalUsers = report["totalUsers"] as Int
        val totalRuntime = report["totalRuntime"] as Int
        val topUsers = report["topUsers"] as List<*>
        val topGpus = report["topGpus"] as List<*>
        val topProjects = report["topProjects"] as List<*>?

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
            content.append("\n")
        }

        if (topProjects != null && topProjects.isNotEmpty()) {
            content.append("📋 项目使用Top10:\n")
            topProjects.take(10).forEachIndexed { index, project ->
                content.append("${index + 1}. ${project.toString()}\n")
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
     * 格式化作息时间分析结果
     */
    fun formatSleepAnalysis(sleepAnalysis: SleepAnalysis): String {
        val content = StringBuilder()
        content.append("\n\n🌙 作息时间分析:\n")
        content.append("====================\n")
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