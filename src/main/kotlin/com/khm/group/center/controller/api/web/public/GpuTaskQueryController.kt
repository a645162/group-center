package com.khm.group.center.controller.api.web.public

import com.khm.group.center.datatype.query.*
import com.khm.group.center.datatype.query.enums.*
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.service.GpuTaskQueryService
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDateTime

/**
 * GPU任务查询控制器
 * 公开接口，不需要权限认证
 */
@RestController
@RequestMapping("/web/open/gpu-tasks")
@Tag(name = "GPU任务查询", description = "公开的GPU任务查询接口，支持多字段灵活查询和分页")
@Slf4jKt
class GpuTaskQueryController {

    @Autowired
    private lateinit var gpuTaskQueryService: GpuTaskQueryService

    /**
     * 高级查询接口（POST方式，支持复杂查询）
     */
    @Operation(
        summary = "GPU任务高级查询",
        description = "支持多字段、多种匹配方式、时间范围、逻辑组合的灵活查询"
    )
    @PostMapping("/query")
    fun queryGpuTasks(@RequestBody request: GpuTaskQueryRequest): ClientResponse {
        logger.info("Received GPU task advanced query request")

        val response = gpuTaskQueryService.queryGpuTasks(request)
        
        val clientResponse = ClientResponse()
        clientResponse.result = response
        clientResponse.isSucceed = true
        
        logger.info("GPU task advanced query completed: ${response.getSummary()}")
        return clientResponse
    }

    /**
     * 简单查询接口（GET方式，支持常用查询参数）
     */
    @Operation(
        summary = "GPU任务简单查询",
        description = "支持常用字段的简单查询，通过URL参数传递"
    )
    @GetMapping("/query")
    fun queryGpuTasksSimple(
        @Parameter(description = "用户名") @RequestParam(required = false) userName: String?,
        @Parameter(description = "项目名（模糊匹配）") @RequestParam(required = false) projectName: String?,
        @Parameter(description = "设备名") @RequestParam(required = false) deviceName: String?,
        @Parameter(description = "任务类型") @RequestParam(required = false) taskType: String?,
        @Parameter(description = "是否多卡任务") @RequestParam(required = false) isMultiGpu: Boolean?,
        @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: LocalDateTime?,
        @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: LocalDateTime?,
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") page: Int,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") pageSize: Int,
        @Parameter(description = "排序字段") @RequestParam(defaultValue = "TASK_START_TIME") sortBy: SortField,
        @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") sortOrder: SortOrder,
        @Parameter(description = "是否包含统计信息") @RequestParam(defaultValue = "false") includeStatistics: Boolean
    ): ClientResponse {
        logger.info("Received GPU task simple query request")

        // 构建查询请求
        val filters = buildFilters(userName, projectName, deviceName, taskType, isMultiGpu)
        val timeRange = buildTimeRange(startTime, endTime)
        val pagination = Pagination(page, pageSize, sortBy, sortOrder)

        val request = GpuTaskQueryRequest(filters, timeRange, pagination, includeStatistics)
        val response = gpuTaskQueryService.queryGpuTasks(request)
        
        val clientResponse = ClientResponse()
        clientResponse.result = response
        clientResponse.isSucceed = true
        
        logger.info("GPU task simple query completed: ${response.getSummary()}")
        return clientResponse
    }

    /**
     * 获取任务总数
     */
    @Operation(summary = "获取任务总数", description = "获取数据库中GPU任务的总数量")
    @GetMapping("/count")
    fun getTotalTaskCount(): ClientResponse {
        logger.info("Received total task count request")

        val count = gpuTaskQueryService.getTotalTaskCount()
        
        val clientResponse = ClientResponse()
        clientResponse.result = mapOf("totalTasks" to count)
        clientResponse.isSucceed = true
        
        logger.info("Total task count query completed: $count")
        return clientResponse
    }

    /**
     * 获取最近N小时的任务
     */
    @Operation(summary = "获取最近任务", description = "获取最近N小时内的GPU任务")
    @GetMapping("/recent")
    fun getRecentTasks(
        @Parameter(description = "小时数", example = "24") @RequestParam(defaultValue = "24") hours: Int
    ): ClientResponse {
        logger.info("Received recent ${hours} hours task request")

        val tasks = gpuTaskQueryService.getRecentTasks(hours)
        
        val clientResponse = ClientResponse()
        clientResponse.result = mapOf(
            "tasks" to tasks,
            "count" to tasks.size,
            "hours" to hours
        )
        clientResponse.isSucceed = true
        
        logger.info("Recent task query completed: found ${tasks.size} records")
        return clientResponse
    }

    /**
     * 获取用户任务统计
     */
    @Operation(summary = "获取用户任务统计", description = "获取指定用户的任务统计信息")
    @GetMapping("/user-stats/{userName}")
    fun getUserTaskStats(
        @Parameter(description = "用户名") @PathVariable userName: String
    ): ClientResponse {
        logger.info("Received user task statistics request: $userName")

        val stats = gpuTaskQueryService.getUserTaskStats(userName)
        
        val clientResponse = ClientResponse()
        clientResponse.result = stats
        clientResponse.isSucceed = true
        
        logger.info("User task statistics completed: $userName")
        return clientResponse
    }

    /**
     * 获取设备任务统计
     */
    @Operation(summary = "获取设备任务统计", description = "获取指定设备的任务统计信息")
    @GetMapping("/device-stats/{deviceName}")
    fun getDeviceTaskStats(
        @Parameter(description = "设备名") @PathVariable deviceName: String
    ): ClientResponse {
        logger.info("Received device task statistics request: $deviceName")

        val stats = gpuTaskQueryService.getDeviceTaskStats(deviceName)
        
        val clientResponse = ClientResponse()
        clientResponse.result = stats
        clientResponse.isSucceed = true
        
        logger.info("Device task statistics completed: $deviceName")
        return clientResponse
    }

    /**
     * 构建过滤器列表
     */
    private fun buildFilters(
        userName: String?,
        projectName: String?,
        deviceName: String?,
        taskType: String?,
        isMultiGpu: Boolean?
    ): List<QueryFilter> {
        val filters = mutableListOf<QueryFilter>()

        userName?.let {
            filters.add(QueryFilter(QueryField.TASK_USER, QueryOperator.EQUALS, it))
        }

        projectName?.let {
            filters.add(QueryFilter(QueryField.PROJECT_NAME, QueryOperator.LIKE, it))
        }

        deviceName?.let {
            filters.add(QueryFilter(QueryField.SERVER_NAME_ENG, QueryOperator.EQUALS, it))
        }

        taskType?.let {
            filters.add(QueryFilter(QueryField.TASK_TYPE, QueryOperator.EQUALS, it))
        }

        isMultiGpu?.let {
            filters.add(QueryFilter(QueryField.IS_MULTI_GPU, QueryOperator.EQUALS, it))
        }

        return filters
    }

    /**
     * 构建时间范围
     */
    private fun buildTimeRange(
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): TimeRange? {
        return if (startTime != null || endTime != null) {
            val startInstant = startTime?.atZone(java.time.ZoneId.systemDefault())?.toInstant()
            val endInstant = endTime?.atZone(java.time.ZoneId.systemDefault())?.toInstant()
            TimeRange(startInstant, endInstant)
        } else {
            null
        }
    }
}