package com.khm.group.center.datatype.query.enums

/**
 * 逻辑操作符枚举
 */
enum class LogicOperator {
    AND,  // 与
    OR;   // 或

    /**
     * 获取逻辑操作符的描述
     */
    fun getDescription(): String {
        return when (this) {
            AND -> "与"
            OR -> "或"
        }
    }
}