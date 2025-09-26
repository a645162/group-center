package com.khm.group.center.datatype.query

import com.khm.group.center.datatype.query.enums.SortField
import com.khm.group.center.datatype.query.enums.SortOrder

/**
 * 分页参数
 */
data class Pagination(
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortBy: SortField = SortField.getDefault(),
    val sortOrder: SortOrder = SortOrder.getDefault()
) {
    /**
     * 验证分页参数的有效性
     */
    fun validate(): Boolean {
        return page > 0 && pageSize > 0 && pageSize <= MAX_PAGE_SIZE
    }

    /**
     * 获取偏移量
     */
    fun getOffset(): Int {
        return (page - 1) * pageSize
    }

    /**
     * 获取排序字段的数据库列名
     */
    fun getSortColumn(): String {
        return sortBy.getColumnName()
    }

    /**
     * 获取排序方向（ASC/DESC）
     */
    fun getSortDirection(): String {
        return sortOrder.name
    }

    companion object {
        const val MAX_PAGE_SIZE = 1000
        const val DEFAULT_PAGE_SIZE = 20
    }
}