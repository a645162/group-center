package com.khm.group.center.datatype.query

/**
 * GPU任务查询请求
 */
data class GpuTaskQueryRequest(
    val filters: List<QueryFilter> = emptyList(),
    val timeRange: TimeRange? = null,
    val pagination: Pagination = Pagination(),
    val includeStatistics: Boolean = false
) {
    /**
     * 验证查询请求的有效性
     */
    fun validate(): Boolean {
        // 验证分页参数
        if (!pagination.validate()) {
            return false
        }

        // 验证时间范围
        if (timeRange != null && !timeRange.validate()) {
            return false
        }

        // 验证所有过滤器
        return filters.all { it.validate() }
    }

    /**
     * 检查是否有查询条件
     */
    fun hasFilters(): Boolean {
        return filters.isNotEmpty() || timeRange != null
    }

    /**
     * 获取查询条件的描述
     */
    fun getQueryDescription(): String {
        val filterDescriptions = filters.joinToString(" ") { it.getDescription() }
        val timeDescription = timeRange?.let { 
            "时间范围: ${it.startTime} 到 ${it.endTime}" 
        } ?: ""
        
        return listOf(filterDescriptions, timeDescription)
            .filter { it.isNotBlank() }
            .joinToString(" | ")
    }
}