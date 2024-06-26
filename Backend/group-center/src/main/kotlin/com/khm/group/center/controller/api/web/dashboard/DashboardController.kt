package com.khm.group.center.controller.api.web.dashboard

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.db.mapper.client.GpuTaskInfoMapper
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import io.swagger.v3.oas.annotations.Operation
import org.jobrunr.scheduling.BackgroundJob
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.*

@RestController
class DashboardController {

    @Autowired
    lateinit var gpuTaskInfoMapper: GpuTaskInfoMapper

    fun queryRecentOneMonthTasks(): List<GpuTaskInfoModel> {
        // 获取当前时间
        val currentTime = Date()
        // 计算一个月前的时间
        val oneMonthAgo = getOneMonthAgoDate(currentTime).time / 1000

        // 创建查询条件
        val queryWrapper = QueryWrapper<GpuTaskInfoModel>()
        // 查询 taskStartTime >= oneMonthAgo 的记录
        queryWrapper.ge("task_start_time", oneMonthAgo)

        // 执行查询
        return gpuTaskInfoMapper.selectList(queryWrapper)
    }

    private fun getOneMonthAgoDate(currentTime: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentTime
        calendar.add(Calendar.MONTH, -1)
        return calendar.time
    }

    @RequestMapping("/web/dashboard/usage/test", method = [RequestMethod.GET])
    fun test(): String {
        BackgroundJob.enqueue({
            println("Current Time: ${Instant.now()}")
        })
//        BackgroundJob.schedule(Instant.now()) {
//            // Print Current Time(Formated)
//            println("Current Time: ${Instant.now()}")
//        }
//        BackgroundJob.schedule<EmailService>(Instant.now(), x -> x.sendNewlyRegisteredEmail());
//        BackgroundJob.scheduleRecurrently(Cron.every15seconds()) { println("Easy!") }
        println("ok")
        return "ok"
    }

    @Operation(summary = "更新面板")
    @RequestMapping("/web/dashboard/usage/update", method = [RequestMethod.GET])
    fun gpuTaskInfo(): ClientResponse {

        val re = queryRecentOneMonthTasks()

        val result = ClientResponse()
        result.result = "success"
        return result
    }

}
