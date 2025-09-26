package com.khm.group.center.datatype.query.enums

/**
 * 查询字段枚举
 * 对应 GpuTaskInfoModel 的所有可查询字段
 */
enum class QueryField {
    // 基础信息
    ID,
    TASK_USER,
    PROJECT_NAME,
    SERVER_NAME_ENG,
    
    // 任务特征
    TASK_TYPE,
    TASK_STATUS,
    MESSAGE_TYPE,
    IS_DEBUG_MODE,
    IS_MULTI_GPU,
    MULTI_DEVICE_WORLD_SIZE,
    MULTI_DEVICE_LOCAL_RANK,
    
    // GPU资源
    GPU_USAGE_PERCENT,
    GPU_MEMORY_PERCENT,
    TASK_GPU_MEMORY_GB,
    TASK_GPU_ID,
    TASK_GPU_NAME,
    
    // 时间相关
    TASK_START_TIME,
    TASK_FINISH_TIME,
    TASK_RUNNING_TIME_IN_SECONDS,
    
    // 技术环境
    PYTHON_VERSION,
    CUDA_VERSION,
    CONDA_ENV_NAME,
    SCREEN_SESSION_NAME,
    COMMAND_LINE,
    
    // 文件路径
    PROJECT_DIRECTORY,
    PY_FILE_NAME;

    /**
     * 获取字段对应的数据库列名
     */
    fun getColumnName(): String {
        return when (this) {
            ID -> "id"
            TASK_USER -> "task_user"
            PROJECT_NAME -> "project_name"
            SERVER_NAME_ENG -> "server_name_eng"
            TASK_TYPE -> "task_type"
            TASK_STATUS -> "task_status"
            MESSAGE_TYPE -> "message_type"
            IS_DEBUG_MODE -> "is_debug_mode"
            IS_MULTI_GPU -> "is_multi_gpu"
            MULTI_DEVICE_WORLD_SIZE -> "multi_device_world_size"
            MULTI_DEVICE_LOCAL_RANK -> "multi_device_local_rank"
            GPU_USAGE_PERCENT -> "gpu_usage_percent"
            GPU_MEMORY_PERCENT -> "gpu_memory_percent"
            TASK_GPU_MEMORY_GB -> "task_gpu_memory_gb"
            TASK_GPU_ID -> "task_gpu_id"
            TASK_GPU_NAME -> "task_gpu_name"
            TASK_START_TIME -> "task_start_time"
            TASK_FINISH_TIME -> "task_finish_time"
            TASK_RUNNING_TIME_IN_SECONDS -> "task_running_time_in_seconds"
            PYTHON_VERSION -> "python_version"
            CUDA_VERSION -> "cuda_version"
            CONDA_ENV_NAME -> "conda_env_name"
            SCREEN_SESSION_NAME -> "screen_session_name"
            COMMAND_LINE -> "command_line"
            PROJECT_DIRECTORY -> "project_directory"
            PY_FILE_NAME -> "py_file_name"
        }
    }

    /**
     * 获取字段的Java/Kotlin属性名
     */
    fun getPropertyName(): String {
        return when (this) {
            ID -> "id"
            TASK_USER -> "taskUser"
            PROJECT_NAME -> "projectName"
            SERVER_NAME_ENG -> "serverNameEng"
            TASK_TYPE -> "taskType"
            TASK_STATUS -> "taskStatus"
            MESSAGE_TYPE -> "messageType"
            IS_DEBUG_MODE -> "isDebugMode"
            IS_MULTI_GPU -> "isMultiGpu"
            MULTI_DEVICE_WORLD_SIZE -> "multiDeviceWorldSize"
            MULTI_DEVICE_LOCAL_RANK -> "multiDeviceLocalRank"
            GPU_USAGE_PERCENT -> "gpuUsagePercent"
            GPU_MEMORY_PERCENT -> "gpuMemoryPercent"
            TASK_GPU_MEMORY_GB -> "taskGpuMemoryGb"
            TASK_GPU_ID -> "taskGpuId"
            TASK_GPU_NAME -> "taskGpuName"
            TASK_START_TIME -> "taskStartTime"
            TASK_FINISH_TIME -> "taskFinishTime"
            TASK_RUNNING_TIME_IN_SECONDS -> "taskRunningTimeInSeconds"
            PYTHON_VERSION -> "pythonVersion"
            CUDA_VERSION -> "cudaVersion"
            CONDA_ENV_NAME -> "condaEnvName"
            SCREEN_SESSION_NAME -> "screenSessionName"
            COMMAND_LINE -> "commandLine"
            PROJECT_DIRECTORY -> "projectDirectory"
            PY_FILE_NAME -> "pyFileName"
        }
    }

    /**
     * 检查字段是否支持模糊查询
     */
    fun supportsLikeOperator(): Boolean {
        return when (this) {
            ID, IS_DEBUG_MODE, IS_MULTI_GPU, 
            GPU_USAGE_PERCENT, GPU_MEMORY_PERCENT, TASK_GPU_MEMORY_GB,
            TASK_GPU_ID, MULTI_DEVICE_WORLD_SIZE, MULTI_DEVICE_LOCAL_RANK,
            TASK_START_TIME, TASK_FINISH_TIME, TASK_RUNNING_TIME_IN_SECONDS -> false
            else -> true
        }
    }

    /**
     * 检查字段是否支持范围查询
     */
    fun supportsRangeOperator(): Boolean {
        return when (this) {
            ID, GPU_USAGE_PERCENT, GPU_MEMORY_PERCENT, TASK_GPU_MEMORY_GB,
            TASK_GPU_ID, MULTI_DEVICE_WORLD_SIZE, MULTI_DEVICE_LOCAL_RANK,
            TASK_START_TIME, TASK_FINISH_TIME, TASK_RUNNING_TIME_IN_SECONDS -> true
            else -> false
        }
    }
}