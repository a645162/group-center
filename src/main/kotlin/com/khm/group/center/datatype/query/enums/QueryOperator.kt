package com.khm.group.center.datatype.query.enums

/**
 * 查询操作符枚举
 */
enum class QueryOperator {
    EQUALS,          // 等于
    NOT_EQUALS,      // 不等于
    LIKE,            // 模糊匹配
    GREATER_THAN,    // 大于
    LESS_THAN,       // 小于
    GREATER_EQUAL,   // 大于等于
    LESS_EQUAL,      // 小于等于
    BETWEEN;         // 介于之间

    /**
     * 检查操作符是否支持指定的字段类型
     */
    fun supportsField(field: QueryField): Boolean {
        return when (this) {
            EQUALS, NOT_EQUALS -> true // 所有字段都支持等于和不等于
            LIKE -> field.supportsLikeOperator()
            GREATER_THAN, LESS_THAN, GREATER_EQUAL, LESS_EQUAL -> field.supportsRangeOperator()
            BETWEEN -> field.supportsRangeOperator()
        }
    }

    /**
     * 获取操作符的描述
     */
    fun getDescription(): String {
        return when (this) {
            EQUALS -> "等于"
            NOT_EQUALS -> "不等于"
            LIKE -> "模糊匹配"
            GREATER_THAN -> "大于"
            LESS_THAN -> "小于"
            GREATER_EQUAL -> "大于等于"
            LESS_EQUAL -> "小于等于"
            BETWEEN -> "介于之间"
        }
    }
}