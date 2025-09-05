package com.khm.group.center.task

import com.khm.group.center.db.analyse.GpuTaskAnalyse
import com.khm.group.center.service.BotPushService
import com.khm.group.center.service.ReportPushService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@EnableScheduling
@SpringJUnitConfig
class ReportSchedulerServiceTest {

    private val gpuTaskAnalyse: GpuTaskAnalyse = mockk(relaxed = true)
    private val botPushService: BotPushService = mockk(relaxed = true)
    private val reportPushService: ReportPushService = mockk(relaxed = true)

    private val reportSchedulerService = ReportSchedulerService(
        gpuTaskAnalyse = gpuTaskAnalyse,
        botPushService = botPushService,
        reportPushService = reportPushService
    )

    @Test
    fun `test pushDailyReportNow calls reportPushService`() {
        // When
        reportSchedulerService.pushDailyReportNow()

        // Then
        verify { reportPushService.pushDailyReport() }
    }

    @Test
    fun `test pushWeeklyReportNow calls reportPushService`() {
        // When
        reportSchedulerService.pushWeeklyReportNow()

        // Then
        verify { reportPushService.pushWeeklyReport() }
    }

    @Test
    fun `test pushMonthlyReportNow calls reportPushService`() {
        // When
        reportSchedulerService.pushMonthlyReportNow()

        // Then
        verify { reportPushService.pushMonthlyReport() }
    }

    @Test
    fun `test pushYearlyReportNow calls reportPushService`() {
        // When
        reportSchedulerService.pushYearlyReportNow()

        // Then
        verify { reportPushService.pushYearlyReport() }
    }

    @Test
    fun `test checkAndPushMissingReports calls reportPushService`() {
        // When
        reportSchedulerService.checkAndPushMissingReports()

        // Then
        verify { reportPushService.checkAndPushMissingReports() }
    }

    @Test
    fun `test updateStatisticsCache calls gpuTaskAnalyse clearCache`() {
        // When
        reportSchedulerService.updateStatisticsCache()

        // Then
        verify { gpuTaskAnalyse.clearCache() }
    }
}