package com.khm.group.center.controller.api.web.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.service.BaseStatisticsService
import com.khm.group.center.service.CachedStatisticsService
import com.khm.group.center.utils.time.TimePeriod
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/web/dashboard/statistics")
class StatisticsController {

    @Autowired
    lateinit var cachedStatisticsService: CachedStatisticsService

    @Autowired
    lateinit var baseStatisticsService: BaseStatisticsService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Operation(summary = "获取用户统计")
    @GetMapping("/users")
    fun getUserStatistics(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = cachedStatisticsService.getUserStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取GPU统计")
    @GetMapping("/gpus")
    fun getGpuStatistics(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = cachedStatisticsService.getGpuStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取服务器统计")
    @GetMapping("/servers")
    fun getServerStatistics(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = cachedStatisticsService.getServerStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取项目统计")
    @GetMapping("/projects")
    fun getProjectStatistics(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = cachedStatisticsService.getProjectStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取时间趋势统计")
    @GetMapping("/time-trend")
    fun getTimeTrendStatistics(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = cachedStatisticsService.getTimeTrendStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取24小时报告（最近24小时使用情况）")
    @GetMapping("/reports/24hour")
    fun get24HourReport(): ClientResponse {
        val report = cachedStatisticsService.get24HourReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取48小时报告（最近48小时使用情况）")
    @GetMapping("/reports/48hour")
    fun get48HourReport(): ClientResponse {
        val report = cachedStatisticsService.get48HourReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取72小时报告（最近72小时使用情况）")
    @GetMapping("/reports/72hour")
    fun get72HourReport(): ClientResponse {
        val report = cachedStatisticsService.get72HourReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取今日日报（今天0:00到明天0:00）")
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

}