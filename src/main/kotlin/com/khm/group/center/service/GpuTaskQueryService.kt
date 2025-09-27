package com.khm.group.center.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.khm.group.center.datatype.query.*
import com.khm.group.center.datatype.query.enums.SortOrder
import com.khm.group.center.db.mapper.client.GpuTaskInfoMapper
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * GPU任务查询服务
 */
@Service
@Slf4jKt
class GpuTaskQueryService {

    @Autowired
    private lateinit var gpuTaskInfoMapper: GpuTaskInfoMapper

    private val queryBuilder = GpuTaskQueryBuilder()
    private val statisticsCalculator = QueryStatisticsCalculator()

    /**
     * 执行GPU任务查询
     */
    fun queryGpuTasks(request: GpuTaskQueryRequest): GpuTaskQueryResponse {
        logger.info("Start GPU task query: ${request.getQueryDescription()}")

        // 验证请求参数
        if (!request.validate()) {
            logger.warn("Query request parameter validation failed")
            return GpuTaskQueryResponse.empty()
        }

        try {
            // 构建查询条件
            val queryWrapper = queryBuilder.buildQueryWrapper(request.filters, request.timeRange)

            // 设置排序
            applySorting(queryWrapper, request.pagination)

            // 执行分页查询（手动分页方式）
            val pageResult = executePagedQuery(queryWrapper, request.pagination)

            // 计算统计信息（如果需要）
            val statistics = if (request.includeStatistics) {
                statisticsCalculator.calculateStatistics(pageResult.records)
            } else {
                null
            }

            // 构建响应
            val paginationInfo = PaginationInfo.fromPagination(request.pagination, pageResult.total)

            logger.info("Query completed: found ${pageResult.total} records, returned ${pageResult.records.size} records")

            return GpuTaskQueryResponse.fromDataWithStats(
                data = pageResult.records,
                pagination = paginationInfo,
                statistics = statistics ?: QueryStatistics.empty()
            )

        } catch (e: Exception) {
            logger.error("GPU task query failed", e)
            return GpuTaskQueryResponse.empty()
        }
    }

    /**
     * 应用排序
     */
    private fun applySorting(
        queryWrapper: QueryWrapper<GpuTaskInfoModel>,
        pagination: Pagination
    ) {
        val sortColumn = pagination.getSortColumn()
        pagination.getSortDirection()

        when (pagination.sortOrder) {
            SortOrder.ASC -> queryWrapper.orderByAsc(sortColumn)
            SortOrder.DESC -> queryWrapper.orderByDesc(sortColumn)
        }
    }

    /**
     * 执行分页查询（手动分页方式）
     */
    private fun executePagedQuery(
        queryWrapper: QueryWrapper<GpuTaskInfoModel>,
        pagination: Pagination
    ): Page<GpuTaskInfoModel> {
        // 先查询总数
        val totalCount = gpuTaskInfoMapper.selectCount(queryWrapper).toLong()
        
        // 计算总页数
        val totalPages = if (totalCount == 0L) 1 else 
            ((totalCount - 1) / pagination.pageSize + 1).toInt()
        
        // 修正页码，确保在有效范围内
        val correctedPage = if (pagination.page > totalPages) {
            logger.warn("Page number ${pagination.page} out of range (total pages: $totalPages), automatically corrected to last page")
            totalPages
        } else if (pagination.page < 1) {
            logger.warn("Page number ${pagination.page} is invalid, automatically corrected to first page")
            1
        } else {
            pagination.page
        }
        
        // 计算修正后的偏移量
        val correctedOffset = (correctedPage - 1) * pagination.pageSize
        
        // 手动设置分页参数
        queryWrapper.last("LIMIT $correctedOffset, ${pagination.pageSize}")
        
        // 查询当前页数据
        val records = gpuTaskInfoMapper.selectList(queryWrapper)
        
        // 创建分页结果
        val page = Page<GpuTaskInfoModel>(
            correctedPage.toLong(),
            pagination.pageSize.toLong(),
            totalCount
        )
        page.records = records
        
        logger.debug("Pagination query result: total=$totalCount, totalPages=$totalPages, currentPage=$correctedPage, returnedRecords=${records.size}")
        
        return page
    }

    /**
     * 获取所有任务数量（用于统计）
     */
    fun getTotalTaskCount(): Long {
        return gpuTaskInfoMapper.selectCount(null).toLong()
    }

    /**
     * 获取最近N小时的任务
     */
    fun getRecentTasks(hours: Int): List<GpuTaskInfoModel> {
        val timeRange = TimeRange.lastNHours(hours)
        val queryWrapper = queryBuilder.buildQueryWrapper(emptyList(), timeRange)
        queryWrapper.orderByDesc("task_start_time")

        return gpuTaskInfoMapper.selectList(queryWrapper)
    }

    /**
     * 获取用户的任务统计
     */
    fun getUserTaskStats(userName: String): QueryStatistics {
        val filters = listOf(
            QueryFilter(
                field = com.khm.group.center.datatype.query.enums.QueryField.TASK_USER,
                operator = com.khm.group.center.datatype.query.enums.QueryOperator.EQUALS,
                value = userName
            )
        )

        val queryWrapper = queryBuilder.buildQueryWrapper(filters, null)
        val tasks = gpuTaskInfoMapper.selectList(queryWrapper)

        return statisticsCalculator.calculateStatistics(tasks)
    }

    /**
     * 获取设备的任务统计
     */
    fun getDeviceTaskStats(deviceName: String): QueryStatistics {
        val filters = listOf(
            QueryFilter(
                field = com.khm.group.center.datatype.query.enums.QueryField.SERVER_NAME_ENG,
                operator = com.khm.group.center.datatype.query.enums.QueryOperator.EQUALS,
                value = deviceName
            )
        )

        val queryWrapper = queryBuilder.buildQueryWrapper(filters, null)
        val tasks = gpuTaskInfoMapper.selectList(queryWrapper)

        return statisticsCalculator.calculateStatistics(tasks)
    }
}