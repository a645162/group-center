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
        logger.info("开始执行GPU任务查询: ${request.getQueryDescription()}")

        // 验证请求参数
        if (!request.validate()) {
            logger.warn("查询请求参数验证失败")
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

            logger.info("查询完成: 找到 ${pageResult.total} 条记录，返回 ${pageResult.records.size} 条")

            return GpuTaskQueryResponse.fromDataWithStats(
                data = pageResult.records,
                pagination = paginationInfo,
                statistics = statistics ?: QueryStatistics.empty()
            )

        } catch (e: Exception) {
            logger.error("GPU任务查询失败", e)
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
        val sortDirection = pagination.getSortDirection()

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
            logger.warn("页码 ${pagination.page} 超出范围（总页数：$totalPages），自动修正为最后一页")
            totalPages
        } else if (pagination.page < 1) {
            logger.warn("页码 ${pagination.page} 无效，自动修正为第一页")
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
        
        logger.debug("分页查询结果: 总数=$totalCount, 总页数=$totalPages, 当前页=$correctedPage, 返回记录数=${records.size}")
        
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