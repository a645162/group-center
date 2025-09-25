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
     * @return 日报
     */
    fun generate24HourReport(tasks: List<GpuTaskInfoModel>, startTimestamp: Long, endTimestamp: Long): DailyReport

    /**
     * 生成日报（按自然日统计，无缓存）
     * @param tasks 任务列表
     * @param date 日期
     * @return 日报
     */
    fun generateDailyReport(tasks: List<GpuTaskInfoModel>, date: LocalDate): DailyReport

    /**
     * 生成周报（无缓存）
     * @param tasks 任务列表
     * @return 周报
     */
    fun generateWeeklyReport(tasks: List<GpuTaskInfoModel>): WeeklyReport

    /**
     * 生成月报（无缓存）
     * @param tasks 任务列表
     * @return 月报
     */
    fun generateMonthlyReport(tasks: List<GpuTaskInfoModel>): MonthlyReport

    /**
     * 生成年报（无缓存）
     * @param tasks 任务列表
     * @return 年报
     */
    fun generateYearlyReport(tasks: List<GpuTaskInfoModel>): YearlyReport

    /**
     * 获取自定义时间段统计（无缓存）
     * @param tasks 任务列表
     * @param startTime 开始时间（秒）
     * @param endTime 结束时间（秒）
     * @return 自定义时间段统计
     */
    fun getCustomPeriodStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long, endTime: Long): CustomPeriodStatistics

    /**
     * 获取作息时间分析（无缓存）
     * @param tasks 任务列表
     * @param startTime 开始时间（秒）
     * @param endTime 结束时间（秒）
     * @return 作息分析结果
     */
    fun getSleepAnalysis(tasks: List<GpuTaskInfoModel>, startTime: Long, endTime: Long): SleepAnalysis
}