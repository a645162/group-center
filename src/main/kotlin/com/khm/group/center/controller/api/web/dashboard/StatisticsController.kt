package com.khm.group.center.controller.api.web.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.service.StatisticsService
import com.khm.group.center.utils.time.TimePeriod
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/web/dashboard/statistics")
class StatisticsController {

    @Autowired
    lateinit var statisticsService: StatisticsService


    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Operation(summary = "获取用户统计")
    @GetMapping("/users")
    fun getUserStatistics(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = statisticsService.getUserStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取GPU统计")
    @GetMapping("/gpus")
    fun getGpuStatistics(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = statisticsService.getGpuStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取服务器统计")
    @GetMapping("/servers")
    fun getServerStatistics(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = statisticsService.getServerStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取项目统计")
    @GetMapping("/projects")
    fun getProjectStatistics(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = statisticsService.getProjectStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取时间趋势统计")
    @GetMapping("/time-trend")
    fun getTimeTrendStatistics(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = statisticsService.getTimeTrendStatistics(period)
        
        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取24小时报告（最近24小时使用情况）")
    @GetMapping("/reports/24hour")
    fun get24HourReport(): ClientResponse {
        val report = statisticsService.get24HourReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取48小时报告（最近48小时使用情况）")
    @GetMapping("/reports/48hour")
    fun get48HourReport(): ClientResponse {
        val report = statisticsService.get48HourReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取72小时报告（最近72小时使用情况）")
    @GetMapping("/reports/72hour")
    fun get72HourReport(): ClientResponse {
        val report = statisticsService.get72HourReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取日报（按自然日统计）")
    @GetMapping("/reports/daily")
    fun getDailyReport(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ClientResponse {
        val report = statisticsService.getDailyReport(date ?: LocalDate.now())
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取周报")
    @GetMapping("/reports/weekly")
    fun getWeeklyReport(): ClientResponse {
        val report = statisticsService.getWeeklyReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取月报")
    @GetMapping("/reports/monthly")
    fun getMonthlyReport(): ClientResponse {
        val report = statisticsService.getMonthlyReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取年报")
    @GetMapping("/reports/yearly")
    fun getYearlyReport(): ClientResponse {
        val report = statisticsService.getYearlyReport()
        
        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "强制更新统计缓存")
    @PostMapping("/cache/update")
    fun forceUpdateCache(): ClientResponse {
        statisticsService.forceUpdateCache()
        
        val result = ClientResponse()
        result.result = "Statistics cache updated successfully"
        return result
    }

    @Operation(summary = "清除统计缓存")
    @PostMapping("/cache/clear")
    fun clearCache(): ClientResponse {
        statisticsService.clearCache()
        
        val result = ClientResponse()
        result.result = "Statistics cache cleared successfully"
        return result
    }

}