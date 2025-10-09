package com.khm.group.center.service

import com.khm.group.center.datatype.statistics.*
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.utils.time.TimePeriod
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 基础统计服务接口 - 无缓存版本
 * 专注于业务逻辑，不包含任何缓存逻辑
 */
interface BaseStatisticsService {

    /**
     * 获取用户统计信息（无缓存）
     * @param tasks 任务列表
     * @param startTime 开始时间（秒，可选）
     * @param endTime 结束时间（秒，可选）
     * @return 用户统计列表
     */
    fun getUserStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long? = null, endTime: Long? = null): List<UserStatistics>

    /**
     * 获取GPU统计信息（无缓存）
     * @param tasks 任务列表
     * @param startTime 开始时间（秒，可选）
     * @param endTime 结束时间（秒，可选）
     * @return GPU统计列表
     */
    fun getGpuStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long? = null, endTime: Long? = null): List<GpuStatistics>

    /**
     * 获取服务器统计信息（无缓存）
     * @param tasks 任务列表
     * @param startTime 开始时间（秒，可选）
     * @param endTime 结束时间（秒，可选）
     * @return 服务器统计列表
     */
    fun getServerStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long? = null, endTime: Long? = null): List<ServerStatistics>

    /**
     * 获取项目统计信息（无缓存）
     * @param tasks 任务列表
     * @param startTime 开始时间（秒，可选）
     * @param endTime 结束时间（秒，可选）
     * @return 项目统计列表
     */
    fun getProjectStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long? = null, endTime: Long? = null): List<ProjectStatistics>

    /**
     * 获取时间趋势统计信息（无缓存）
     * @param tasks 任务列表
     * @param timePeriod 时间周期
     * @return 时间趋势统计
     */
    fun getTimeTrendStatistics(tasks: List<GpuTaskInfoModel>, timePeriod: TimePeriod): TimeTrendStatistics

    /**
     * 生成24小时报告（无缓存）
     * @param tasks 任务列表
     * @param startTimestamp 开始时间戳（秒）
     * @param endTimestamp 结束时间戳（秒）
     * @return 报告
     */
    fun generate24HourReport(tasks: List<GpuTaskInfoModel>, startTimestamp: Long, endTimestamp: Long): Report

    /**
     * 生成日报（按自然日统计，无缓存）
     * @param tasks 任务列表
     * @param date 日期（可选，默认昨天）
     * @return 报告
     */
    fun generateDailyReport(tasks: List<GpuTaskInfoModel>, date: LocalDate? = null): Report

    /**
     * 生成指定日期范围的日报（无缓存）
     * @param tasks 任务列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告
     */
    fun generateDailyReport(tasks: List<GpuTaskInfoModel>, startDate: LocalDate, endDate: LocalDate): Report

    /**
     * 生成周报（无缓存）
     * @param tasks 任务列表
     * @param year 年份（可选，默认当前年）
     * @param week 周数（可选，默认当前周）
     * @return 报告
     */
    fun generateWeeklyReport(tasks: List<GpuTaskInfoModel>, year: Int? = null, week: Int? = null): Report

    /**
     * 生成月报（无缓存）
     * @param tasks 任务列表
     * @param year 年份（可选，默认当前年）
     * @param month 月份（可选，默认当前月）
     * @return 报告
     */
    fun generateMonthlyReport(tasks: List<GpuTaskInfoModel>, year: Int? = null, month: Int? = null): Report

    /**
     * 生成年报（无缓存）
     * @param tasks 任务列表
     * @param year 年份（可选，默认当前年）
     * @return 报告
     */
    fun generateYearlyReport(tasks: List<GpuTaskInfoModel>, year: Int? = null): Report

    /**
     * 获取作息时间分析（无缓存）
     * @param tasks 任务列表
     * @param startTime 开始时间（秒）
     * @param endTime 结束时间（秒）
     * @return 作息分析结果
     */
    fun getSleepAnalysis(tasks: List<GpuTaskInfoModel>, startTime: Long, endTime: Long): SleepAnalysis

    /**
     * 获取自定义时间段统计（无缓存）
     * @param tasks 任务列表
     * @param startTime 开始时间（秒）
     * @param endTime 结束时间（秒）
     * @return 报告
     */
    fun getCustomPeriodStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long, endTime: Long): Report

    /**
     * 获取用户活动时间分布（无缓存）
     * 统计每个用户的活动时间段，以4点为分界线处理跨天时间区间
     * @param tasks 任务列表
     * @param startTime 开始时间（秒，可选）
     * @param endTime 结束时间（秒，可选）
     * @return 用户活动时间分布
     */
    fun getUserActivityTimeDistribution(
        tasks: List<GpuTaskInfoModel>,
        startTime: Long? = null,
        endTime: Long? = null
    ): UserActivityTimeDistribution
}