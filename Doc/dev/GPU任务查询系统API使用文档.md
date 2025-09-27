# GPU任务查询系统API使用文档

## 概述
GPU任务查询系统提供了灵活的查询接口，支持多字段组合查询、分页和统计功能。所有接口均为公开接口，不需要权限认证。

## API端点

### 1. 高级查询接口（POST）
- **URL**: `POST /web/open/gpu-tasks/query`
- **描述**: 支持复杂查询条件的高级接口
- **请求体**: JSON格式的查询请求

**请求示例**:
```json
{
  "filters": [
    {
      "field": "TASK_USER",
      "operator": "EQUALS",
      "value": "konghaomin",
      "logic": "AND"
    },
    {
      "field": "PROJECT_NAME", 
      "operator": "LIKE",
      "value": "ai",
      "logic": "AND"
    }
  ],
  "timeRange": {
    "startTime": "2025-09-01T00:00:00",
    "endTime": "2025-09-26T23:59:59"
  },
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "sortBy": "TASK_START_TIME",
    "sortOrder": "DESC"
  },
  "includeStatistics": true
}
```

### 2. 简单查询接口（GET）
- **URL**: `GET /web/open/gpu-tasks/query`
- **描述**: 支持常用查询参数的简单接口

**查询参数**:
- `userName`: 用户名（完全匹配）
- `projectName`: 项目名（模糊匹配）
- `deviceName`: 设备名（完全匹配）
- `taskType`: 任务类型
- `isMultiGpu`: 是否多卡任务
- `startTime`: 开始时间（ISO格式）
- `endTime`: 结束时间（ISO格式）
- `page`: 页码（默认1）
- `pageSize`: 每页大小（默认20）
- `sortBy`: 排序字段
- `sortOrder`: 排序方向（ASC/DESC）
- `includeStatistics`: 是否包含统计信息

**使用示例**:
```
GET /web/open/gpu-tasks/query?userName=konghaomin&projectName=ai&page=1&pageSize=20&includeStatistics=true
```

### 3. 获取任务总数
- **URL**: `GET /web/open/gpu-tasks/count`
- **描述**: 获取数据库中GPU任务的总数量

### 4. 获取最近任务
- **URL**: `GET /web/open/gpu-tasks/recent?hours=24`
- **描述**: 获取最近N小时内的任务
- **参数**: `hours` - 小时数（默认24）

### 5. 用户任务统计
- **URL**: `GET /web/open/gpu-tasks/user-stats/{userName}`
- **描述**: 获取指定用户的任务统计信息

### 6. 设备任务统计
- **URL**: `GET /web/open/gpu-tasks/device-stats/{deviceName}`
- **描述**: 获取指定设备的任务统计信息

## 支持的查询字段

### 基础信息字段
- `ID`: 任务ID（完全匹配）
- `TASK_USER`: 用户名（完全匹配）
- `PROJECT_NAME`: 项目名（模糊匹配）
- `SERVER_NAME_ENG`: 设备名（完全匹配）

### 任务特征字段
- `TASK_TYPE`: 任务类型
- `TASK_STATUS`: 任务状态
- `MESSAGE_TYPE`: 消息类型
- `IS_DEBUG_MODE`: 是否调试模式
- `IS_MULTI_GPU`: 是否多卡任务
- `MULTI_DEVICE_WORLD_SIZE`: 多卡并行规模
- `MULTI_DEVICE_LOCAL_RANK`: 多卡任务中的卡序号

### GPU资源字段
- `GPU_USAGE_PERCENT`: GPU使用率（范围查询）
- `GPU_MEMORY_PERCENT`: 显存使用率（范围查询）
- `TASK_GPU_MEMORY_GB`: 显存占用大小（范围查询）
- `TASK_GPU_ID`: GPU卡ID
- `TASK_GPU_NAME`: GPU卡名称

### 时间相关字段
- `TASK_START_TIME`: 任务开始时间（范围查询）
- `TASK_FINISH_TIME`: 任务结束时间（范围查询）
- `TASK_RUNNING_TIME_IN_SECONDS`: 运行时长（范围查询）

### 技术环境字段
- `PYTHON_VERSION`: Python版本
- `CUDA_VERSION`: CUDA版本
- `CONDA_ENV_NAME`: Conda环境
- `SCREEN_SESSION_NAME`: Screen会话
- `COMMAND_LINE`: 命令行（模糊匹配）

### 文件路径字段
- `PROJECT_DIRECTORY`: 项目目录
- `PY_FILE_NAME`: Python文件名

## 支持的查询操作符

- `EQUALS`: 等于
- `NOT_EQUALS`: 不等于
- `LIKE`: 模糊匹配（支持%通配符）
- `GREATER_THAN`: 大于
- `LESS_THAN`: 小于
- `GREATER_EQUAL`: 大于等于
- `LESS_EQUAL`: 小于等于
- `BETWEEN`: 介于之间（需要两个值）

## 响应格式

### 查询响应结构
```json
{
  "serverVersion": "1.0.0",
  "isSucceed": true,
  "result": {
    "data": [...], // 任务数据列表
    "pagination": {
      "currentPage": 1,
      "pageSize": 20,
      "totalPages": 5,
      "totalItems": 95
    },
    "statistics": {
      "totalTasks": 95,
      "totalRunningTime": 86400,
      "avgGpuUsage": 85.5,
      "maxGpuUsage": 99.0,
      "multiGpuTaskCount": 10,
      "userDistribution": {...},
      "projectDistribution": {...}
    }
  }
}
```

### 统计信息说明
- `totalTasks`: 总任务数
- `totalRunningTime`: 总运行时长（秒）
- `avgGpuUsage`: 平均GPU使用率
- `maxGpuUsage`: 最大GPU使用率
- `multiGpuTaskCount`: 多卡任务数量
- `userDistribution`: 用户分布统计
- `projectDistribution`: 项目分布统计
- `deviceDistribution`: 设备分布统计

## 使用示例

### 示例1：查询特定用户的高负载任务
```json
{
  "filters": [
    {"field": "TASK_USER", "operator": "EQUALS", "value": "konghaomin"},
    {"field": "GPU_USAGE_PERCENT", "operator": "GREATER_THAN", "value": 80}
  ],
  "pagination": {"sortBy": "GPU_USAGE_PERCENT", "sortOrder": "DESC"}
}
```

### 示例2：查询多卡任务的统计信息
```json
{
  "filters": [
    {"field": "IS_MULTI_GPU", "operator": "EQUALS", "value": true}
  ],
  "includeStatistics": true
}
```

### 示例3：查询时间范围内的调试任务
```json
{
  "filters": [
    {"field": "IS_DEBUG_MODE", "operator": "EQUALS", "value": true}
  ],
  "timeRange": {
    "startTime": "2025-09-01T00:00:00",
    "endTime": "2025-09-26T23:59:59"
  }
}
```

## 注意事项

1. **公开接口**: 所有接口均为公开接口，不需要权限认证
2. **性能优化**: 建议合理使用分页，避免查询大量数据
3. **字段支持**: 不同字段支持的操作符不同，请参考字段说明
4. **时间格式**: 时间参数使用ISO 8601格式
5. **错误处理**: 所有接口都有完善的错误处理机制

## Swagger文档

启动应用后，可以通过以下地址访问Swagger文档：
- `http://localhost:8080/swagger-ui.html`

在Swagger文档中可以查看详细的API说明和进行在线测试。