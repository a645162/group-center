package com.khm.group.center.db.query

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.khm.group.center.db.mapper.client.GpuTaskInfoMapper
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.utils.time.TimePeriod
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class GpuTaskQuery {

    @Autowired
    lateinit var gpuTaskInfoMapper: GpuTaskInfoMapper

    fun queryTasks(
        timePeriod: TimePeriod,
        userName: String = "",
        serverNameEng: String = "",
        onlyFinished: Boolean = true,
        startTime: Long? = null,
        endTime: Long? = null
    ): List<GpuTaskInfoModel> {
        // Create Query Wrapper
        val queryWrapper = QueryWrapper<GpuTaskInfoModel>()

        // 处理时间范围查询 - 改进的逻辑：选择与统计区间有重叠的任务
        val actualStartTime: Long
        val actualEndTime: Long
        
        if (startTime != null && endTime != null) {
            // 自定义时间范围
            actualStartTime = startTime
            actualEndTime = endTime
        } else {
            // 使用预定义的时间周期
            actualStartTime = timePeriod.getAgoTimestamp(null) / 1000
            actualEndTime = System.currentTimeMillis() / 1000
        }

        // 选择与统计区间有重叠的任务：
        // 1. 任务在统计区间内开始
        // 2. 任务在统计区间内结束
        // 3. 任务跨越统计区间（开始时间 < 统计区间开始，结束时间 > 统计区间结束）
        queryWrapper.and { wrapper ->
            wrapper.or { or1 ->
                // 任务在统计区间内开始
                or1.ge("task_start_time", actualStartTime)
                    .le("task_start_time", actualEndTime)
            }.or { or2 ->
                // 任务在统计区间内结束
                or2.ge("task_finish_time", actualStartTime)
                    .le("task_finish_time", actualEndTime)
            }.or { or3 ->
                // 任务跨越统计区间
                or3.le("task_start_time", actualStartTime)
                    .ge("task_finish_time", actualEndTime)
            }
        }

        if (userName.isNotEmpty()) {
            queryWrapper.eq("task_user", userName)
        }

        if (serverNameEng.isNotEmpty()) {
            queryWrapper.eq("server_name_eng", serverNameEng)
        }

        if (onlyFinished) {
            queryWrapper.eq("message_type", "finish")
        }

        // Query
        return gpuTaskInfoMapper.selectList(queryWrapper)
    }

}
