package com.khm.group.center.controller.api.web.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.db.analyse.GpuTaskAnalyse
import com.khm.group.center.db.query.GpuTaskQuery
import com.khm.group.center.utils.time.TimePeriod
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/web/dashboard")
class DashboardController {

    @Autowired
    lateinit var gpuTaskQuery: GpuTaskQuery

    @Autowired
    lateinit var gpuTaskAnalyse: GpuTaskAnalyse

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Operation(summary = "更新面板")
    @RequestMapping("/usage/update", method = [RequestMethod.GET])
    fun gpuTaskInfo(): ClientResponse {
        gpuTaskQuery.queryTasks(TimePeriod.ONE_WEEK)

        val result = ClientResponse()
        result.result = "success"
        return result
    }

    @Operation(summary = "获取用户统计")
    @GetMapping("/stats/users")
    fun getUserStats(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = gpuTaskAnalyse.getUserStats(period)

        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取GPU统计")
    @GetMapping("/stats/gpus")
    fun getGpuStats(@RequestParam(defaultValue = "ONE_WEEK") timePeriod: String): ClientResponse {
        val period = TimePeriod.valueOf(timePeriod)
        val stats = gpuTaskAnalyse.getGpuStats(period)

        val result = ClientResponse()
        result.result = stats
        return result
    }

    @Operation(summary = "获取日报")
    @GetMapping("/reports/daily")
    fun getDailyReport(): ClientResponse {
        val report = gpuTaskAnalyse.getDailyReport()

        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取周报")
    @GetMapping("/reports/weekly")
    fun getWeeklyReport(): ClientResponse {
        val report = gpuTaskAnalyse.getWeeklyReport()

        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取月报")
    @GetMapping("/reports/monthly")
    fun getMonthlyReport(): ClientResponse {
        val report = gpuTaskAnalyse.getMonthlyReport()

        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "获取年报")
    @GetMapping("/reports/yearly")
    fun getYearlyReport(): ClientResponse {
        val report = gpuTaskAnalyse.getYearlyReport()

        val result = ClientResponse()
        result.result = report
        return result
    }

    @Operation(summary = "清除统计缓存")
    @PostMapping("/cache/clear")
    fun clearCache(): ClientResponse {
        gpuTaskAnalyse.clearCache()

        val result = ClientResponse()
        result.result = "Cache cleared successfully"
        return result
    }
}
