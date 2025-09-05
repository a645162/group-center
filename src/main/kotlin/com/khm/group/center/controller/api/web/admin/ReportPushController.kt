package com.khm.group.center.controller.api.web.admin

import com.khm.group.center.task.ReportSchedulerService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/report/push")
class ReportPushController(
    private val reportSchedulerService: ReportSchedulerService
) {

    @PostMapping("/daily/now")
    fun pushDailyReportNow(): String {
        reportSchedulerService.pushDailyReportNow()
        return "Daily report pushed successfully"
    }

    @PostMapping("/weekly/now")
    fun pushWeeklyReportNow(): String {
        reportSchedulerService.pushWeeklyReportNow()
        return "Weekly report pushed successfully"
    }

    @PostMapping("/monthly/now")
    fun pushMonthlyReportNow(): String {
        reportSchedulerService.pushMonthlyReportNow()
        return "Monthly report pushed successfully"
    }

    @PostMapping("/yearly/now")
    fun pushYearlyReportNow(): String {
        reportSchedulerService.pushYearlyReportNow()
        return "Yearly report pushed successfully"
    }

    @PostMapping("/check-missing")
    fun checkAndPushMissingReports(): String {
        reportSchedulerService.checkAndPushMissingReports()
        return "Missing reports check completed"
    }

    @PostMapping("/update-cache")
    fun updateStatisticsCache(): String {
        reportSchedulerService.updateStatisticsCache()
        return "Statistics cache updated"
    }
}