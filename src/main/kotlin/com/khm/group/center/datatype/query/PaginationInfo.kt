package com.khm.group.center.datatype.query

/**
 * 分页信息
 */
data class PaginationInfo(
    val currentPage: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Long
) {
    /**
     * 检查是否有上一页
     */
    fun hasPrevious(): Boolean {
        return currentPage > 1
    }

    /**
     * 检查是否有下一页
     */
    fun hasNext(): Boolean {
        return currentPage < totalPages
    }

    /**
     * 获取上一页页码
     */
    fun getPreviousPage(): Int? {
        return if (hasPrevious()) currentPage - 1 else null
    }

    /**
     * 获取下一页页码
     */
    fun getNextPage(): Int? {
        return if (hasNext()) currentPage + 1 else null
    }

    /**
     * 获取当前页的起始项目索引
     */
    fun getStartIndex(): Long {
        return (currentPage - 1).toLong() * pageSize + 1
    }

    /**
     * 获取当前页的结束项目索引
     */
    fun getEndIndex(): Long {
        val endIndex = currentPage.toLong() * pageSize
        return if (endIndex > totalItems) totalItems else endIndex
    }

    companion object {
        /**
         * 从分页参数和总项目数创建分页信息
         */
        fun fromPagination(pagination: Pagination, totalItems: Long): PaginationInfo {
            val totalPages = if (totalItems == 0L) 1 else 
                ((totalItems - 1) / pagination.pageSize + 1).toInt()
            
            return PaginationInfo(
                currentPage = pagination.page,
                pageSize = pagination.pageSize,
                totalPages = totalPages,
                totalItems = totalItems
            )
        }
    }
}