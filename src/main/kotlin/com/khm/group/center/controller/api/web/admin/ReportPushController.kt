package com.khm.group.center.controller.api.web.admin

import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.task.ReportSchedulerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/report/push")
@Tag(name = "Report Push", description = "Report push management API for manual report triggering")
class ReportPushController(
    private val reportSchedulerService: ReportSchedulerService
) {

    @Operation(summary = "立即推送今日日报")
    @PostMapping("/today/now")
    fun pushTodayReportNow(): ClientResponse {
        reportSchedulerService.pushTodayReportNow()
        
        val result = ClientResponse()
        result.result = mapOf(
            "message" to "Today report pushed successfully",
            "status" to "completed"
        )
        return result
    }

    @Operation(summary = "立即推送昨日日报")
    @PostMapping("/yesterday/now")
    fun pushYesterdayReportNow(): ClientResponse {
        reportSchedulerService.pushYesterdayReportNow()
        
        val result = ClientResponse()
        result.result = mapOf(
            "message" to "Yesterday report pushed successfully",
            "status" to "completed"
        )
        return result
    }

    @Operation(summary = "立即推送周报")
    @PostMapping("/weekly/now")
    fun pushWeeklyReportNow(): ClientResponse {
        reportSchedulerService.pushWeeklyReportNow()
        
        val result = ClientResponse()
        result.result = mapOf(
            "message" to "Weekly report pushed successfully",
            "status" to "completed"
        )
        return result
    }

    @Operation(summary = "立即推送月报")
    @PostMapping("/monthly/now")
    fun pushMonthlyReportNow(): ClientResponse {
        reportSchedulerService.pushMonthlyReportNow()
        
        val result = ClientResponse()
        result.result = mapOf(
            "message" to "Monthly report pushed successfully",
            "status" to "completed"
        )
        return result
    }

    @Operation(summary = "立即推送年报")
    @PostMapping("/yearly/now")
    fun pushYearlyReportNow(): ClientResponse {
        reportSchedulerService.pushYearlyReportNow()
        
        val result = ClientResponse()
        result.result = mapOf(
            "message" to "Yearly report pushed successfully",
            "status" to "completed"
        )
        return result
    }


    @Operation(summary = "更新统计缓存")
    @PostMapping("/update-cache")
    fun updateStatisticsCache(): ClientResponse {
        reportSchedulerService.updateStatisticsCache()
        
        val result = ClientResponse()
        result.result = mapOf(
            "message" to "Statistics cache updated",
            "status" to "completed"
        )
        return result
    }
}