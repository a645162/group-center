package com.khm.group.center.datatype.query.enums

/**
 * 排序字段枚举
 */
enum class SortField {
    ID,
    TASK_USER,
    PROJECT_NAME,
    SERVER_NAME_ENG,
    TASK_START_TIME,
    TASK_FINISH_TIME,
    TASK_RUNNING_TIME_IN_SECONDS,
    GPU_USAGE_PERCENT,
    GPU_MEMORY_PERCENT,
    TASK_GPU_MEMORY_GB;

    /**
     * 获取排序字段对应的数据库列名
     */
    fun getColumnName(): String {
        return when (this) {
            ID -> "id"
            TASK_USER -> "task_user"
            PROJECT_NAME -> "project_name"
            SERVER_NAME_ENG -> "server_name_eng"
            TASK_START_TIME -> "task_start_time"
            TASK_FINISH_TIME -> "task_finish_time"
            TASK_RUNNING_TIME_IN_SECONDS -> "task_running_time_in_seconds"
            GPU_USAGE_PERCENT -> "gpu_usage_percent"
            GPU_MEMORY_PERCENT -> "gpu_memory_percent"
            TASK_GPU_MEMORY_GB -> "task_gpu_memory_gb"
        }
    }

    /**
     * 获取默认排序字段
     */
    companion object {
        fun getDefault(): SortField = TASK_START_TIME
    }
}