package com.khm.group.center.controller.api.web.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.service.BaseStatisticsService
import com.khm.group.center.service.CachedStatisticsService
import com.khm.group.center.utils.time.TimePeriod
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/web/dashboard/statistics")
@Tag(name = "Dashboard Statistics", description = "Dashboard statistics API for user, GPU, server, and project statistics")
class StatisticsController {

    @Autowired
    lateinit var cachedStatisticsService: CachedStatisticsService

    @Autowired
    lateinit var baseStatisticsService: BaseStatisticsService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Operation(
        summary = "Get User Statistics",
        description = "Retrieve user statistics including active users, task counts, and usage patterns for the specified time period"
    )
    @GetMapping("/users")
    fun getUserStatistics(
        @Parameter(description = "Time period for statistics (default: ONE_WEEK)", example = "ONE_WEEK")
        @RequestParam(defaultValue = "ONE_WEEK") timePeriod: String
    ): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = cachedStatisticsService.getUserStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(
        summary = "Get GPU Statistics",
        description = "Retrieve GPU usage statistics including utilization rates, task distribution, and performance metrics"
    )
    @GetMapping("/gpus")
    fun getGpuStatistics(
        @Parameter(description = "Time period for statistics (default: ONE_WEEK)", example = "ONE_WEEK")
        @RequestParam(defaultValue = "ONE_WEEK") timePeriod: String
    ): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = cachedStatisticsService.getGpuStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(
        summary = "Get Server Statistics",
        description = "Retrieve server statistics including machine status, availability, and resource usage across all servers"
    )
    @GetMapping("/servers")
    fun getServerStatistics(
        @Parameter(description = "Time period for statistics (default: ONE_WEEK)", example = "ONE_WEEK")
        @RequestParam(defaultValue = "ONE_WEEK") timePeriod: String
    ): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = cachedStatisticsService.getServerStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(
        summary = "Get Project Statistics",
        description = "Retrieve project statistics including project counts, task distribution, and usage patterns"
    )
    @GetMapping("/projects")
    fun getProjectStatistics(
        @Parameter(description = "Time period for statistics (default: ONE_WEEK)", example = "ONE_WEEK")
        @RequestParam(defaultValue = "ONE_WEEK") timePeriod: String
    ): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = cachedStatisticsService.getProjectStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(
        summary = "Get Time Trend Statistics",
        description = "Retrieve time-based trend statistics showing usage patterns over time (hourly/daily trends)"
    )
    @GetMapping("/time-trend")
    fun getTimeTrendStatistics(
        @Parameter(description = "Time period for trend analysis (default: ONE_WEEK)", example = "ONE_WEEK")
        @RequestParam(defaultValue = "ONE_WEEK") timePeriod: String
    ): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = cachedStatisticsService.getTimeTrendStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(
        summary = "Get 24-Hour Report",
        description = "Retrieve comprehensive usage report for the last 24 hours including task counts, user activity, and resource utilization"
    )
    @GetMapping("/reports/24hour")
    fun get24HourReport(): ClientResponse {
        val report = cachedStatisticsService.get24HourReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(
        summary = "Get 48-Hour Report",
        description = "Retrieve comprehensive usage report for the last 48 hours with extended trend analysis"
    )
    @GetMapping("/reports/48hour")
    fun get48HourReport(): ClientResponse {
        val report = cachedStatisticsService.get48HourReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(
        summary = "Get 72-Hour Report",
        description = "Retrieve comprehensive usage report for the last 72 hours with detailed trend analysis and patterns"
    )
    @GetMapping("/reports/72hour")
    fun get72HourReport(): ClientResponse {
        val report = cachedStatisticsService.get72HourReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(
        summary = "Get Today's Report",
        description = "Retrieve daily report for the current day (from 00:00 to 23:59)"
    )
    @GetMapping("/reports/today")
    fun getTodayReport(): ClientResponse {
        val report = cachedStatisticsService.getTodayReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取昨日日报（昨天0:00到今天0:00）")
    @GetMapping("/reports/yesterday")
    fun getYesterdayReport(): ClientResponse {
        val report = cachedStatisticsService.getYesterdayReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取周报")
    @GetMapping("/reports/weekly")
    fun getWeeklyReport(): ClientResponse {
        val report = cachedStatisticsService.getWeeklyReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取月报")
    @GetMapping("/reports/monthly")
    fun getMonthlyReport(): ClientResponse {
        val report = cachedStatisticsService.getMonthlyReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取年报")
    @GetMapping("/reports/yearly")
    fun getYearlyReport(): ClientResponse {
        val report = cachedStatisticsService.getYearlyReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取自定义时间段统计")
    @GetMapping("/custom")
    fun getCustomPeriodStatistics(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: LocalDateTime
    ): ClientResponse {
        // 使用基础服务（无缓存）进行自定义时间段统计
        val startTimestamp = startTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        val endTimestamp = endTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        
        val tasks = (baseStatisticsService as com.khm.group.center.service.StatisticsServiceImpl)
            .getTasksByCustomPeriod(startTimestamp, endTimestamp)
        val stats = baseStatisticsService.getCustomPeriodStatistics(tasks, startTimestamp, endTimestamp)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取作息时间分析")
    @GetMapping("/sleep-analysis")
    fun getSleepAnalysis(
        @RequestParam(defaultValue = "ONE_WEEK") timePeriod: String
    ): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        
        // 使用基础服务（无缓存）进行作息分析
        val tasks = (baseStatisticsService as com.khm.group.center.service.StatisticsServiceImpl)
            .getTasksByTimePeriod(period)
        
        // 计算时间段的开始和结束时间
        val currentTime = System.currentTimeMillis() / 1000
        val startTime = period.getAgoTimestamp(null) / 1000
        
        val sleepAnalysis = baseStatisticsService.getSleepAnalysis(tasks, startTime, currentTime)
        
        val result = ClientResponse()
        result.result = sleepAnalysis
        return result
    }

    @Operation(summary = "强制更新统计缓存")
    @PostMapping("/cache/update")
    fun forceUpdateCache(): ClientResponse {
        cachedStatisticsService.forceUpdateCache()
        
        val result = ClientResponse()
        result.result = "Statistics cache updated successfully"
        return result
    }

    @Operation(summary = "清除统计缓存")
    @PostMapping("/cache/clear")
    fun clearCache(): ClientResponse {
        cachedStatisticsService.clearCache()
        
        val result = ClientResponse()
        result.result = "Statistics cache cleared successfully"
        return result
    }

    @Operation(
        summary = "获取用户活动时间分布",
        description = """
            统计每个用户的活动时间段，以4点为分界线处理跨天时间区间。
            特殊处理逻辑：
            - 如果用户的活动时间跨越4点，需要特殊处理跨天区间
            - 例如：用户6点启动，3点启动，活动区间为6:00-3:00（跨天）
            - 4点之前的时间视为第二天的对应时间进行计算
        """
    )
    @GetMapping("/user-activity-time-distribution")
    fun getUserActivityTimeDistribution(
        @Parameter(description = "Time period for statistics (default: ONE_WEEK)", example = "ONE_WEEK")
        @RequestParam(defaultValue = "ONE_WEEK") timePeriod: String
    ): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        
        // 使用缓存服务获取用户活动时间分布
        val distribution = cachedStatisticsService.getUserActivityTimeDistribution(period)
        
        val result = ClientResponse()
        result.result = distribution
        return result
    }

    @Operation(
        summary = "获取自定义时间段用户活动时间分布",
        description = """
            统计指定时间段内用户的活动时间段，以4点为分界线处理跨天时间区间。
            特殊处理逻辑：
            - 如果用户的活动时间跨越4点，需要特殊处理跨天区间
            - 例如：用户6点启动，3点启动，活动区间为6:00-3:00（跨天）
            - 4点之前的时间视为第二天的对应时间进行计算
        """
    )
    @GetMapping("/user-activity-time-distribution/custom")
    fun getUserActivityTimeDistributionCustom(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: LocalDateTime
    ): ClientResponse {
        val startTimestamp = startTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        val endTimestamp = endTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        
        // 使用缓存服务获取自定义时间段用户活动时间分布
        val distribution = cachedStatisticsService.getUserActivityTimeDistribution(startTimestamp, endTimestamp)
        
        val result = ClientResponse()
        result.result = distribution
        return result
    }

}