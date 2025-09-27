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
@Tag(name = "GPU Task Query", description = "Public GPU task query interface, supports multi-field flexible query and pagination")
@Slf4jKt
class GpuTaskQueryController {

    @Autowired
    private lateinit var gpuTaskQueryService: GpuTaskQueryService

    /**
     * 高级查询接口（POST方式，支持复杂查询）
     */
    @Operation(
        summary = "GPU Task Advanced Query",
        description = """
            Advanced GPU task query with flexible filtering, time range, and logical combinations.
            Supports complex query conditions including:
            - Multiple field filters with different operators (equals, like, contains, etc.)
            - Time range filtering (start time and end time)
            - Pagination with customizable page size and sorting
            - Logical combinations (AND/OR) of multiple conditions
            - Statistics inclusion option for query results
        """
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
        summary = "GPU Task Simple Query",
        description = """
            Simple GPU task query with commonly used parameters via URL query string.
            Provides basic filtering capabilities for quick searches without complex request body.
            Supports filtering by user, project, device, task type, and time range.
        """
    )
    @GetMapping("/query")
    fun queryGpuTasksSimple(
        @Parameter(description = "Username for filtering tasks") @RequestParam(required = false) userName: String?,
        @Parameter(description = "Project name (supports fuzzy matching)") @RequestParam(required = false) projectName: String?,
        @Parameter(description = "Device name for filtering") @RequestParam(required = false) deviceName: String?,
        @Parameter(description = "Task type filter") @RequestParam(required = false) taskType: String?,
        @Parameter(description = "Filter by multi-GPU tasks (true/false)") @RequestParam(required = false) isMultiGpu: Boolean?,
        @Parameter(description = "Start time for time range filtering (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: LocalDateTime?,
        @Parameter(description = "End time for time range filtering (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: LocalDateTime?,
        @Parameter(description = "Page number for pagination (default: 1)") @RequestParam(defaultValue = "1") page: Int,
        @Parameter(description = "Page size for pagination (default: 20)") @RequestParam(defaultValue = "20") pageSize: Int,
        @Parameter(description = "Sort field (default: TASK_START_TIME)") @RequestParam(defaultValue = "TASK_START_TIME") sortBy: SortField,
        @Parameter(description = "Sort direction (default: DESC)") @RequestParam(defaultValue = "DESC") sortOrder: SortOrder,
        @Parameter(description = "Include statistics in response (default: false)") @RequestParam(defaultValue = "false") includeStatistics: Boolean
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
    @Operation(
        summary = "Get Total Task Count",
        description = "Retrieve the total number of GPU tasks in the database"
    )
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
    @Operation(
        summary = "Get Recent Tasks",
        description = "Retrieve GPU tasks from the last N hours"
    )
    @GetMapping("/recent")
    fun getRecentTasks(
        @Parameter(description = "Number of hours to look back (default: 24)", example = "24") @RequestParam(defaultValue = "24") hours: Int
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
    @Operation(
        summary = "Get User Task Statistics",
        description = "Retrieve detailed statistics for a specific user's GPU tasks"
    )
    @GetMapping("/user-stats/{userName}")
    fun getUserTaskStats(
        @Parameter(description = "Username to get statistics for") @PathVariable userName: String
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
    @Operation(
        summary = "Get Device Task Statistics",
        description = "Retrieve detailed statistics for GPU tasks on a specific device"
    )
    @GetMapping("/device-stats/{deviceName}")
    fun getDeviceTaskStats(
        @Parameter(description = "Device name to get statistics for") @PathVariable deviceName: String
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