@file:Suppress("UNCHECKED_CAST")

package com.khm.group.center.db.analyse

import com.khm.group.center.datatype.summary.GpuSummary
import com.khm.group.center.datatype.summary.GpuTaskDetail
import com.khm.group.center.datatype.summary.PersonSummary
import com.khm.group.center.datatype.summary.TaskSummary
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.db.query.GpuTaskQuery
import com.khm.group.center.utils.time.TimePeriod
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Repository
class GpuTaskAnalyse {
    @Autowired
    lateinit var gpuTaskQuery: GpuTaskQuery

    // 缓存统计数据
    private val dailyStatsCache = ConcurrentHashMap<String, Any>()
    private val weeklyStatsCache = ConcurrentHashMap<String, Any>()
    private val monthlyStatsCache = ConcurrentHashMap<String, Any>()
    private val yearlyStatsCache = ConcurrentHashMap<String, Any>()

    // 最后更新时间
    private var lastUpdateTime: Long = 0
    private val CACHE_DURATION = 60 * 60 * 1000L // 1小时缓存

    // Gpu列表
    val gpuList = mutableListOf<GpuSummary>()

    // Gpu型号列表
    val gpuModelList = mutableListOf<GpuSummary>()

    // 用户列表
    val userList = mutableListOf<PersonSummary>()

    // 熬夜冠军(最晚睡的人，截止到凌晨4点)
    var dailySleepLateChampion: PersonSummary? = null

    /**
     * 获取用户统计数据
     */
    fun getUserStats(timePeriod: TimePeriod): List<PersonSummary> {
        val cacheKey = "user_stats_${timePeriod}"
        val cached = getCachedData(timePeriod, cacheKey)
        if (cached != null) {
            return cached as List<PersonSummary>
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val userStats = analyzeUserStats(tasks)

        cacheData(timePeriod, cacheKey, userStats)
        return userStats
    }

    /**
     * 获取GPU统计数据
     */
    fun getGpuStats(timePeriod: TimePeriod): List<GpuSummary> {
        val cacheKey = "gpu_stats_${timePeriod}"
        val cached = getCachedData(timePeriod, cacheKey)
        if (cached != null) {
            return cached as List<GpuSummary>
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val gpuStats = analyzeGpuStats(tasks)

        cacheData(timePeriod, cacheKey, gpuStats)
        return gpuStats
    }

    /**
     * 获取日报数据
     */
    fun getDailyReport(): Map<String, Any> {
        val cacheKey = "daily_report"
        val cached = getCachedData(TimePeriod.ONE_WEEK, cacheKey)
        if (cached != null) {
            return cached as Map<String, Any>
        }

        val todayTasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_WEEK)
        val report = generateDailyReport(todayTasks)

        cacheData(TimePeriod.ONE_WEEK, cacheKey, report)
        return report
    }

    /**
     * 获取周报数据
     */
    fun getWeeklyReport(): Map<String, Any> {
        val cacheKey = "weekly_report"
        val cached = getCachedData(TimePeriod.ONE_MONTH, cacheKey)
        if (cached != null) {
            return cached as Map<String, Any>
        }

        val weekTasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_MONTH)
        val report = generateWeeklyReport(weekTasks)

        cacheData(TimePeriod.ONE_MONTH, cacheKey, report)
        return report
    }

    /**
     * 获取月报数据
     */
    fun getMonthlyReport(): Map<String, Any> {
        val cacheKey = "monthly_report"
        val cached = getCachedData(TimePeriod.SIX_MONTH, cacheKey)
        if (cached != null) {
            return cached as Map<String, Any>
        }

        val monthTasks = gpuTaskQuery.queryTasks(TimePeriod.SIX_MONTH)
        val report = generateMonthlyReport(monthTasks)

        cacheData(TimePeriod.SIX_MONTH, cacheKey, report)
        return report
    }

    /**
     * 获取年报数据
     */
    fun getYearlyReport(): Map<String, Any> {
        val cacheKey = "yearly_report"
        val cached = getCachedData(TimePeriod.ONE_YEAR, cacheKey)
        if (cached != null) {
            return cached as Map<String, Any>
        }

        val yearTasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_YEAR)
        val report = generateYearlyReport(yearTasks)

        cacheData(TimePeriod.ONE_YEAR, cacheKey, report)
        return report
    }

    private fun analyzeUserStats(tasks: List<GpuTaskInfoModel>): List<PersonSummary> {
        val userMap = mutableMapOf<String, PersonSummary>()

        for (task in tasks) {
            val userName = task.taskUser
            val user = userMap.getOrPut(userName) {
                PersonSummary(userName, userName, 0)
            }

            user.personUseTime += task.taskRunningTimeInSeconds

            // 统计用户最喜欢的GPU（累计统计）
            "${task.taskGpuName}_${task.serverName}"
            val existingGpuSummary = user.gpuModelUseTime.find {
                it.gpuName == task.taskGpuName && it.machineName == task.serverName
            }
            
            if (existingGpuSummary != null) {
                existingGpuSummary.addUseTime(task.taskRunningTimeInSeconds)
            } else {
                val gpuSummary = GpuSummary(task.taskGpuName, task.serverName, 0)
                gpuSummary.addUseTime(task.taskRunningTimeInSeconds)
                user.gpuModelUseTime.add(gpuSummary)
            }

            // 添加详细的任务信息（新增）
            val taskDetail = GpuTaskDetail(
                taskId = task.taskId,
                gpuName = task.taskGpuName,
                machineName = task.serverName,
                gpuUseTime = task.taskRunningTimeInSeconds,
                startTime = task.taskStartTime,
                finishTime = task.taskFinishTime,
                projectName = task.projectName,
                gpuUsagePercent = task.gpuUsagePercent,
                gpuMemoryPercent = task.gpuMemoryPercent,
                gpuMemoryGb = task.taskGpuMemoryGb,
                taskStatus = task.taskStatus
            )
            user.gpuTaskDetails.add(taskDetail)
        }

        return userMap.values.sortedByDescending { it.personUseTime }
    }

    private fun analyzeGpuStats(tasks: List<GpuTaskInfoModel>): List<GpuSummary> {
        val gpuMap = mutableMapOf<String, GpuSummary>()

        for (task in tasks) {
            val gpuKey = "${task.taskGpuName}_${task.serverName}"
            val gpu = gpuMap.getOrPut(gpuKey) {
                GpuSummary(task.taskGpuName, task.serverName, 0)
            }

            gpu.addUseTime(task.taskRunningTimeInSeconds)
        }

        return gpuMap.values.sortedByDescending { it.gpuUseTime }
    }

    private fun generateDailyReport(tasks: List<GpuTaskInfoModel>): Map<String, Any> {
        val todayTasks = tasks.filter { task ->
            val taskDate = Date(task.taskStartTime * 1000)
            val today = Date()
            taskDate.date == today.date && taskDate.month == today.month && taskDate.year == today.year
        }

        return mapOf(
            "date" to Date().toString(),
            "totalTasks" to todayTasks.size,
            "totalUsers" to todayTasks.map { it.taskUser }.distinct().size,
            "totalRuntime" to todayTasks.sumOf { it.taskRunningTimeInSeconds },
            "topUsers" to analyzeUserStats(todayTasks).take(5),
            "topGpus" to analyzeGpuStats(todayTasks).take(5)
        )
    }

    private fun generateWeeklyReport(tasks: List<GpuTaskInfoModel>): Map<String, Any> {
        return mapOf(
            "period" to "weekly",
            "totalTasks" to tasks.size,
            "totalUsers" to tasks.map { it.taskUser }.distinct().size,
            "totalRuntime" to tasks.sumOf { it.taskRunningTimeInSeconds },
            "topUsers" to analyzeUserStats(tasks).take(10),
            "topGpus" to analyzeGpuStats(tasks).take(10)
        )
    }

    private fun generateMonthlyReport(tasks: List<GpuTaskInfoModel>): Map<String, Any> {
        return mapOf(
            "period" to "monthly",
            "totalTasks" to tasks.size,
            "totalUsers" to tasks.map { it.taskUser }.distinct().size,
            "totalRuntime" to tasks.sumOf { it.taskRunningTimeInSeconds },
            "topUsers" to analyzeUserStats(tasks).take(10),
            "topGpus" to analyzeGpuStats(tasks).take(10)
        )
    }

    private fun generateYearlyReport(tasks: List<GpuTaskInfoModel>): Map<String, Any> {
        return mapOf(
            "period" to "yearly",
            "totalTasks" to tasks.size,
            "totalUsers" to tasks.map { it.taskUser }.distinct().size,
            "totalRuntime" to tasks.sumOf { it.taskRunningTimeInSeconds },
            "topUsers" to analyzeUserStats(tasks).take(20),
            "topGpus" to analyzeGpuStats(tasks).take(20)
        )
    }

    private fun getCachedData(timePeriod: TimePeriod, key: String): Any? {
        val cache = when (timePeriod) {
            TimePeriod.ONE_WEEK -> dailyStatsCache
            TimePeriod.ONE_MONTH -> weeklyStatsCache
            TimePeriod.SIX_MONTH -> monthlyStatsCache
            TimePeriod.ONE_YEAR -> yearlyStatsCache
            else -> dailyStatsCache
        }

        return if (System.currentTimeMillis() - lastUpdateTime < CACHE_DURATION) {
            cache[key]
        } else {
            null
        }
    }

    private fun cacheData(timePeriod: TimePeriod, key: String, data: Any) {
        val cache = when (timePeriod) {
            TimePeriod.ONE_WEEK -> dailyStatsCache
            TimePeriod.ONE_MONTH -> weeklyStatsCache
            TimePeriod.SIX_MONTH -> monthlyStatsCache
            TimePeriod.ONE_YEAR -> yearlyStatsCache
            else -> dailyStatsCache
        }

        cache[key] = data
        lastUpdateTime = System.currentTimeMillis()
    }

    /**
     * 清除所有缓存
     */
    fun clearCache() {
        dailyStatsCache.clear()
        weeklyStatsCache.clear()
        monthlyStatsCache.clear()
        yearlyStatsCache.clear()
        lastUpdateTime = 0
    }
}
