package com.khm.group.center.datatype.query.enums

/**
 * 排序方向枚举
 */
enum class SortOrder {
    ASC,   // 升序
    DESC;  // 降序

    /**
     * 获取排序方向的描述
     */
    fun getDescription(): String {
        return when (this) {
            ASC -> "升序"
            DESC -> "降序"
        }
    }

    /**
     * 获取默认排序方向
     */
    companion object {
        fun getDefault(): SortOrder = DESC
    }
}