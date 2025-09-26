package com.khm.group.center.datatype.query

import com.khm.group.center.datatype.query.enums.LogicOperator
import com.khm.group.center.datatype.query.enums.QueryField
import com.khm.group.center.datatype.query.enums.QueryOperator

/**
 * 查询过滤器
 */
data class QueryFilter(
    val field: QueryField,
    val operator: QueryOperator,
    val value: Any,
    val logic: LogicOperator = LogicOperator.AND
) {
    /**
     * 验证过滤器的有效性
     */
    fun validate(): Boolean {
        // 检查操作符是否支持该字段
        if (!operator.supportsField(field)) {
            return false
        }

        // 检查值的类型是否匹配字段类型
        return when (field) {
            QueryField.ID,
            QueryField.TASK_GPU_ID,
            QueryField.MULTI_DEVICE_WORLD_SIZE,
            QueryField.MULTI_DEVICE_LOCAL_RANK,
            QueryField.TASK_RUNNING_TIME_IN_SECONDS -> value is Int || value is Long
            
            QueryField.GPU_USAGE_PERCENT,
            QueryField.GPU_MEMORY_PERCENT,
            QueryField.TASK_GPU_MEMORY_GB -> value is Float || value is Double || value is Int
            
            QueryField.IS_DEBUG_MODE,
            QueryField.IS_MULTI_GPU -> value is Boolean
            
            QueryField.TASK_START_TIME,
            QueryField.TASK_FINISH_TIME -> value is Long || value is String
            
            else -> value is String
        }
    }

    /**
     * 获取过滤器的描述
     */
    fun getDescription(): String {
        return "${field.name} ${operator.getDescription()} $value"
    }
}