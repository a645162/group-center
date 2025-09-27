package com.khm.group.center.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.khm.group.center.datatype.query.QueryFilter
import com.khm.group.center.datatype.query.TimeRange
import com.khm.group.center.datatype.query.enums.LogicOperator
import com.khm.group.center.datatype.query.enums.QueryOperator
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger

/**
 * GPU任务查询构建器
 */
@Slf4jKt
class GpuTaskQueryBuilder {

    /**
     * 构建查询条件
     */
    fun buildQueryWrapper(
        filters: List<QueryFilter>,
        timeRange: TimeRange?
    ): QueryWrapper<GpuTaskInfoModel> {
        val queryWrapper = QueryWrapper<GpuTaskInfoModel>()

        // 处理时间范围
        timeRange?.let { range ->
            buildTimeRangeQuery(queryWrapper, range)
        }

        // 处理过滤器
        if (filters.isNotEmpty()) {
            buildFilterQuery(queryWrapper, filters)
        }

        logger.debug("Build query conditions: ${queryWrapper.targetSql}")
        return queryWrapper
    }

    /**
     * 构建时间范围查询
     */
    private fun buildTimeRangeQuery(
        queryWrapper: QueryWrapper<GpuTaskInfoModel>,
        timeRange: TimeRange
    ) {
        timeRange.getStartTimestamp()?.let { startTime ->
            queryWrapper.ge("task_start_time", startTime)
        }
        
        timeRange.getEndTimestamp()?.let { endTime ->
            queryWrapper.le("task_start_time", endTime)
        }
    }

    /**
     * 构建过滤器查询
     */
    private fun buildFilterQuery(
        queryWrapper: QueryWrapper<GpuTaskInfoModel>,
        filters: List<QueryFilter>
    ) {
        var currentLogic = LogicOperator.AND
        
        filters.forEachIndexed { index, filter ->
            val columnName = filter.field.getColumnName()
            
            when (filter.operator) {
                QueryOperator.EQUALS -> {
                    addCondition(queryWrapper, currentLogic, columnName, "=", filter.value)
                }
                QueryOperator.NOT_EQUALS -> {
                    addCondition(queryWrapper, currentLogic, columnName, "<>", filter.value)
                }
                QueryOperator.LIKE -> {
                    addLikeCondition(queryWrapper, currentLogic, columnName, filter.value)
                }
                QueryOperator.GREATER_THAN -> {
                    addCondition(queryWrapper, currentLogic, columnName, ">", filter.value)
                }
                QueryOperator.LESS_THAN -> {
                    addCondition(queryWrapper, currentLogic, columnName, "<", filter.value)
                }
                QueryOperator.GREATER_EQUAL -> {
                    addCondition(queryWrapper, currentLogic, columnName, ">=", filter.value)
                }
                QueryOperator.LESS_EQUAL -> {
                    addCondition(queryWrapper, currentLogic, columnName, "<=", filter.value)
                }
                QueryOperator.BETWEEN -> {
                    // BETWEEN 操作符需要特殊处理
                    if (filter.value is List<*>) {
                        val values = filter.value as List<*>
                        if (values.size == 2) {
                            addBetweenCondition(queryWrapper, currentLogic, columnName, values[0], values[1])
                        }
                    }
                }
            }
            
            // 更新逻辑操作符用于下一个条件
            currentLogic = filter.logic
        }
    }

    /**
     * 添加普通条件
     */
    private fun addCondition(
        queryWrapper: QueryWrapper<GpuTaskInfoModel>,
        logic: LogicOperator,
        column: String,
        operator: String,
        value: Any
    ) {
        when (logic) {
            LogicOperator.AND -> queryWrapper.and { it.applyCondition(column, operator, value) }
            LogicOperator.OR -> queryWrapper.or { it.applyCondition(column, operator, value) }
        }
    }

    /**
     * 添加模糊查询条件
     */
    private fun addLikeCondition(
        queryWrapper: QueryWrapper<GpuTaskInfoModel>,
        logic: LogicOperator,
        column: String,
        value: Any
    ) {
        val likeValue = "%$value%"
        when (logic) {
            LogicOperator.AND -> queryWrapper.and { it.like(column, likeValue) }
            LogicOperator.OR -> queryWrapper.or { it.like(column, likeValue) }
        }
    }

    /**
     * 添加范围查询条件
     */
    private fun addBetweenCondition(
        queryWrapper: QueryWrapper<GpuTaskInfoModel>,
        logic: LogicOperator,
        column: String,
        value1: Any?,
        value2: Any?
    ) {
        if (value1 != null && value2 != null) {
            when (logic) {
                LogicOperator.AND -> queryWrapper.and { it.between(column, value1, value2) }
                LogicOperator.OR -> queryWrapper.or { it.between(column, value1, value2) }
            }
        }
    }

    /**
     * QueryWrapper的扩展函数，用于应用条件
     */
    private fun QueryWrapper<GpuTaskInfoModel>.applyCondition(column: String, operator: String, value: Any) {
        when (operator) {
            "=" -> this.eq(column, value)
            "<>" -> this.ne(column, value)
            ">" -> this.gt(column, value)
            "<" -> this.lt(column, value)
            ">=" -> this.ge(column, value)
            "<=" -> this.le(column, value)
        }
    }
}