package com.khm.group.center.datatype.query

import com.khm.group.center.db.model.client.GpuTaskInfoModel

/**
 * GPU任务查询响应
 */
data class GpuTaskQueryResponse(
    val data: List<GpuTaskInfoModel>,
    val pagination: PaginationInfo,
    val statistics: QueryStatistics? = null
) {
    /**
     * 获取查询结果的简要信息
     */
    fun getSummary(): String {
        return "查询到 ${pagination.totalItems} 条记录，当前显示第 ${pagination.currentPage}/${pagination.totalPages} 页"
    }

    companion object {
        /**
         * 创建空的查询响应
         */
        fun empty(): GpuTaskQueryResponse {
            return GpuTaskQueryResponse(
                data = emptyList(),
                pagination = PaginationInfo(
                    currentPage = 1,
                    pageSize = 20,
                    totalPages = 1,
                    totalItems = 0
                )
            )
        }

        /**
         * 从数据列表和分页信息创建响应
         */
        fun fromData(data: List<GpuTaskInfoModel>, pagination: PaginationInfo): GpuTaskQueryResponse {
            return GpuTaskQueryResponse(
                data = data,
                pagination = pagination
            )
        }

        /**
         * 从数据列表、分页信息和统计信息创建响应
         */
        fun fromDataWithStats(
            data: List<GpuTaskInfoModel>, 
            pagination: PaginationInfo,
            statistics: QueryStatistics
        ): GpuTaskQueryResponse {
            return GpuTaskQueryResponse(
                data = data,
                pagination = pagination,
                statistics = statistics
            )
        }
    }
}