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

        // 处理时间范围查询
        if (startTime != null && endTime != null) {
            // 自定义时间范围
            queryWrapper.ge("task_start_time", startTime)
            queryWrapper.le("task_start_time", endTime)
        } else {
            // 使用预定义的时间周期
            val timestamp = timePeriod.getAgoTimestamp(null) / 1000
            queryWrapper.ge("task_start_time", timestamp)
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
