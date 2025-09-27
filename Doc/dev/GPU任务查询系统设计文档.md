# GPU任务查询系统设计文档

## 概述
设计一个不需要权限认证的GPU任务查询系统，支持多字段灵活查询、分页和统计功能。

## 数据库结构
基于 `gpu_task_info` 表，包含完整的GPU任务信息字段。

## 查询功能设计

### 支持的查询字段
- **基础信息**: id, taskUser, projectName, serverNameEng
- **任务特征**: taskType, taskStatus, messageType, isDebugMode, isMultiGpu
- **GPU资源**: gpuUsagePercent, gpuMemoryPercent, taskGpuMemoryGb
- **时间相关**: taskStartTime, taskFinishTime, taskRunningTimeInSeconds
- **技术环境**: pythonVersion, cudaVersion, condaEnvName
- **文件路径**: projectDirectory, pyFileName, commandLine

### 查询操作符
- EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, LESS_THAN, BETWEEN

### 逻辑关系
- AND, OR 逻辑组合

## API设计

### 查询接口
```
GET/POST /web/open/gpu-tasks/query
```

### 请求参数
```json
{
  "filters": [
    {
      "field": "字段名",
      "operator": "操作符", 
      "value": "值",
      "logic": "AND/OR"
    }
  ],
  "timeRange": {
    "startTime": "开始时间",
    "endTime": "结束时间"
  },
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "sortBy": "排序字段",
    "sortOrder": "DESC/ASC"
  }
}
```

### 响应格式
```json
{
  "data": [...],
  "pagination": {
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 5,
    "totalItems": 95
  },
  "statistics": {
    "totalGpuUsage": 85.5,
    "avgRunningTime": 3600,
    "multiGpuTaskCount": 10
  }
}
```

## 技术实现

### 文件结构
```
src/main/kotlin/com/khm/group/center/
├── controller/api/web/public/
│   └── GpuTaskQueryController.kt
├── datatype/query/
│   ├── GpuTaskQueryRequest.kt
│   ├── GpuTaskQueryResponse.kt
│   ├── QueryFilter.kt
│   └── enums/
│       ├── QueryField.kt
│       ├── QueryOperator.kt
│       └── LogicOperator.kt
└── service/
    └── GpuTaskQueryService.kt
```

### 核心组件
1. **查询解析器**: 解析请求参数，构建查询条件
2. **查询构建器**: 动态构建MyBatis Plus查询
3. **分页处理器**: 处理分页逻辑
4. **统计计算器**: 计算查询结果的统计信息

## 安全考虑
- 使用 `/web/open/` 路径避开权限拦截器
- 参数验证防止SQL注入
- 查询结果数量限制防止资源耗尽

## 性能优化
- 数据库索引优化
- 分页查询避免大数据量传输
- 统计结果缓存策略