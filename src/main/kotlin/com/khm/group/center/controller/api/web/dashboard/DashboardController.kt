package com.khm.group.center.controller.api.web.dashboard

import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.db.query.GpuTaskQuery
import com.khm.group.center.utils.time.TimePeriod
import io.swagger.v3.oas.annotations.Operation
import org.jobrunr.scheduling.BackgroundJob
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class DashboardController {

    @Autowired
    lateinit var gpuTaskQuery: GpuTaskQuery

    @Operation(summary = "更新面板")
    @RequestMapping("/web/dashboard/usage/update", method = [RequestMethod.GET])
    fun gpuTaskInfo(): ClientResponse {

        gpuTaskQuery.queryTasks(TimePeriod.ONE_WEEK)

        //        BackgroundJob.enqueue({
//            println("Current Time: ${Instant.now()}")
//        })
//        BackgroundJob.schedule(Instant.now()) {
//            // Print Current Time(Formated)
//            println("Current Time: ${Instant.now()}")
//        }
//        BackgroundJob.schedule<EmailService>(Instant.now(), x -> x.sendNewlyRegisteredEmail());
//        BackgroundJob.scheduleRecurrently(Cron.every15seconds()) { println("Easy!") }

        val result = ClientResponse()
        result.result = "success"
        return result
    }

}
