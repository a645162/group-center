package com.khm.group.center.utils.time

import com.khm.group.center.db.model.client.GpuTaskInfoModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 时间分析工具类
 * 用于分析任务的作息时间特征（熬夜、早起等）
 */
@Component
class TimeAnalysisUtils {

    companion object {
        private val logger = LoggerFactory.getLogger(TimeAnalysisUtils::class.java)
        
        // 时间边界定义（小时）
        const val LATE_NIGHT_START_HOUR = 0  // 凌晨0点
        const val LATE_NIGHT_END_HOUR = 4    // 凌晨4点（不含4点）
        const val EARLY_MORNING_START_HOUR = 4   // 凌晨4点（含4点）
        const val EARLY_MORNING_END_HOUR = 10    // 上午10点（不含10点）
    }

    /**
     * 将时间戳转换为本地日期时间
     */
    private fun timestampToLocalDateTime(timestamp: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault())
    }

    /**
     * 判断是否为熬夜时间（00:00-04:00之间，不含4点）
     * @param startTime 任务启动时间（秒）
     * @return 是否为熬夜
     */
    fun isLateNight(startTime: Long): Boolean {
        val localDateTime = timestampToLocalDateTime(startTime)
        val hour = localDateTime.hour
        return hour >= LATE_NIGHT_START_HOUR && hour < LATE_NIGHT_END_HOUR
    }

    /**
     * 判断是否为早起时间（04:00-10:00之间，含4点不含10点）
     * @param startTime 任务启动时间（秒）
     * @return 是否为早起
     */
    fun isEarlyMorning(startTime: Long): Boolean {
        val localDateTime = timestampToLocalDateTime(startTime)
        val hour = localDateTime.hour
        return hour >= EARLY_MORNING_START_HOUR && hour < EARLY_MORNING_END_HOUR
    }

    /**
     * 判断是否为正常时间（10:00-24:00之间，以及00:00之前的正常时间）
     * @param startTime 任务启动时间（秒）
     * @return 是否为正常时间
     */
    fun isNormalTime(startTime: Long): Boolean {
        val localDateTime = timestampToLocalDateTime(startTime)
        val hour = localDateTime.hour
        return hour >= EARLY_MORNING_END_HOUR || hour < LATE_NIGHT_START_HOUR
    }

    /**
     * 获取熬夜任务列表
     * @param tasks 任务列表
     * @param startTime 统计开始时间（秒，可选）
     * @param endTime 统计结束时间（秒，可选）
     * @return 熬夜任务列表
     */
    fun getLateNightTasks(
        tasks: List<GpuTaskInfoModel>,
        startTime: Long? = null,
        endTime: Long? = null
    ): List<GpuTaskInfoModel> {
        return tasks.filter { task ->
            // 检查时间范围
            val inTimeRange = if (startTime != null && endTime != null) {
                task.taskStartTime >= startTime && task.taskStartTime <= endTime
            } else {
                true
            }
            
            inTimeRange && isLateNight(task.taskStartTime)
        }
    }

    /**
     * 获取早起任务列表
     * @param tasks 任务列表
     * @param startTime 统计开始时间（秒，可选）
     * @param endTime 统计结束时间（秒，可选）
     * @return 早起任务列表
     */
    fun getEarlyMorningTasks(
        tasks: List<GpuTaskInfoModel>,
        startTime: Long? = null,
        endTime: Long? = null
    ): List<GpuTaskInfoModel> {
        return tasks.filter { task ->
            // 检查时间范围
            val inTimeRange = if (startTime != null && endTime != null) {
                task.taskStartTime >= startTime && task.taskStartTime <= endTime
            } else {
                true
            }
            
            inTimeRange && isEarlyMorning(task.taskStartTime)
        }
    }

    /**
     * 获取熬夜冠军（最晚启动的任务）
     * @param tasks 任务列表
     * @param startTime 统计开始时间（秒，可选）
     * @param endTime 统计结束时间（秒，可选）
     * @return 熬夜冠军任务，如果没有熬夜任务则返回null
     */
    fun getLateNightChampion(
        tasks: List<GpuTaskInfoModel>,
        startTime: Long? = null,
        endTime: Long? = null
    ): GpuTaskInfoModel? {
        val lateNightTasks = getLateNightTasks(tasks, startTime, endTime)
        if (lateNightTasks.isEmpty()) {
            logger.debug("没有找到熬夜任务")
            return null
        }
        
        // 找到启动时间最晚的任务（即熬夜最晚的）
        return lateNightTasks.maxByOrNull { it.taskStartTime }
    }

    /**
     * 获取早起冠军（最早启动的任务）
     * @param tasks 任务列表
     * @param startTime 统计开始时间（秒，可选）
     * @param endTime 统计结束时间（秒，可选）
     * @return 早起冠军任务，如果没有早起任务则返回null
     */
    fun getEarlyMorningChampion(
        tasks: List<GpuTaskInfoModel>,
        startTime: Long? = null,
        endTime: Long? = null
    ): GpuTaskInfoModel? {
        val earlyMorningTasks = getEarlyMorningTasks(tasks, startTime, endTime)
        if (earlyMorningTasks.isEmpty()) {
            logger.debug("没有找到早起任务")
            return null
        }
        
        // 找到启动时间最早的任务（即起床最早的）
        return earlyMorningTasks.minByOrNull { it.taskStartTime }
    }

    /**
     * 获取作息时间分析结果
     * @param tasks 任务列表
     * @param startTime 统计开始时间（秒）
     * @param endTime 统计结束时间（秒）
     * @return 作息分析结果
     */
    fun analyzeSleepPattern(
        tasks: List<GpuTaskInfoModel>,
        startTime: Long,
        endTime: Long
    ): com.khm.group.center.datatype.statistics.SleepAnalysis {
        logger.info("开始分析作息时间，时间段：$startTime - $endTime，任务数量：${tasks.size}")
        
        val lateNightTasks = getLateNightTasks(tasks, startTime, endTime)
        val earlyMorningTasks = getEarlyMorningTasks(tasks, startTime, endTime)
        
        val lateNightChampion = getLateNightChampion(tasks, startTime, endTime)
        val earlyMorningChampion = getEarlyMorningChampion(tasks, startTime, endTime)
        
        // 收集熬夜和早起的用户
        val lateNightUsers = lateNightTasks.map { it.taskUser }.toSet()
        val earlyMorningUsers = earlyMorningTasks.map { it.taskUser }.toSet()
        
        logger.info("作息分析完成：熬夜任务${lateNightTasks.size}个，早起任务${earlyMorningTasks.size}个")
        
        return com.khm.group.center.datatype.statistics.SleepAnalysis(
            lateNightTasks = lateNightTasks,
            earlyMorningTasks = earlyMorningTasks,
            lateNightChampion = lateNightChampion,
            earlyMorningChampion = earlyMorningChampion,
            totalLateNightTasks = lateNightTasks.size,
            totalEarlyMorningTasks = earlyMorningTasks.size,
            lateNightUsers = lateNightUsers,
            earlyMorningUsers = earlyMorningUsers
        )
    }

    /**
     * 获取时间段的描述信息
     * @param startTime 开始时间（秒）
     * @param endTime 结束时间（秒）
     * @return 时间段描述
     */
    fun getTimePeriodDescription(startTime: Long, endTime: Long): String {
        val startDateTime = timestampToLocalDateTime(startTime)
        val endDateTime = timestampToLocalDateTime(endTime)
        
        return "${startDateTime} 至 ${endDateTime}"
    }

    /**
     * 获取任务的作息时间标签
     * @param task 任务
     * @return 作息时间标签（熬夜、早起、正常）
     */
    fun getSleepTimeLabel(task: GpuTaskInfoModel): String {
        return when {
            isLateNight(task.taskStartTime) -> "熬夜"
            isEarlyMorning(task.taskStartTime) -> "早起"
            else -> "正常"
        }
    }
}