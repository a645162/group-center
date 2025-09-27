# GPU任务查询系统测试指南

## 测试环境准备

### 1. 启动应用
```bash
# 编译并启动应用
.\gradlew bootRun

# 或者使用Docker启动
docker-compose up -d
```

### 2. 验证应用状态
应用启动后，可以通过以下方式验证：
- 健康检查: `http://localhost:8080/actuator/health`
- Swagger文档: `http://localhost:8080/swagger-ui.html`

## 测试用例

### 测试1：基础功能测试

#### 1.1 获取任务总数
```bash
curl -X GET "http://localhost:8080/web/open/gpu-tasks/count"
```

**预期结果**:
```json
{
  "serverVersion": "1.0.0",
  "isSucceed": true,
  "result": {
    "totalTasks": 1234
  }
}
```

#### 1.2 获取最近24小时任务
```bash
curl -X GET "http://localhost:8080/web/open/gpu-tasks/recent?hours=24"
```

### 测试2：简单查询测试

#### 2.1 按用户名查询
```bash
curl -X GET "http://localhost:8080/web/open/gpu-tasks/query?userName=konghaomin&page=1&pageSize=10"
```

#### 2.2 按项目名模糊查询
```bash
curl -X GET "http://localhost:8080/web/open/gpu-tasks/query?projectName=ai&page=1&pageSize=10"
```

#### 2.3 按设备名查询
```bash
curl -X GET "http://localhost:8080/web/open/gpu-tasks/query?deviceName=gpu-server-01&page=1&pageSize=10"
```

### 测试3：组合查询测试

#### 3.1 用户+项目组合查询
```bash
curl -X GET "http://localhost:8080/web/open/gpu-tasks/query?userName=konghaomin&projectName=ai&page=1&pageSize=10"
```

#### 3.2 时间范围查询
```bash
curl -X GET "http://localhost:8080/web/open/gpu-tasks/query?startTime=2025-09-01T00:00:00&endTime=2025-09-26T23:59:59&page=1&pageSize=10"
```

### 测试4：高级查询测试（POST方式）

#### 4.1 复杂条件查询
```bash
curl -X POST "http://localhost:8080/web/open/gpu-tasks/query" \
  -H "Content-Type: application/json" \
  -d '{
    "filters": [
      {
        "field": "TASK_USER",
        "operator": "EQUALS", 
        "value": "konghaomin",
        "logic": "AND"
      },
      {
        "field": "GPU_USAGE_PERCENT",
        "operator": "GREATER_THAN",
        "value": 80,
        "logic": "AND"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "sortBy": "GPU_USAGE_PERCENT",
      "sortOrder": "DESC"
    },
    "includeStatistics": true
  }'
```

#### 4.2 多卡任务统计查询
```bash
curl -X POST "http://localhost:8080/web/open/gpu-tasks/query" \
  -H "Content-Type: application/json" \
  -d '{
    "filters": [
      {
        "field": "IS_MULTI_GPU",
        "operator": "EQUALS",
        "value": true
      }
    ],
    "includeStatistics": true
  }'
```

### 测试5：统计功能测试

#### 5.1 用户任务统计
```bash
curl -X GET "http://localhost:8080/web/open/gpu-tasks/user-stats/konghaomin"
```

#### 5.2 设备任务统计
```bash
curl -X GET "http://localhost:8080/web/open/gpu-tasks/device-stats/gpu-server-01"
```

## 测试验证要点

### 响应结构验证
1. **基础响应**: 检查 `isSucceed` 是否为 `true`
2. **分页信息**: 验证 `pagination` 字段的正确性
3. **数据格式**: 检查返回的任务数据格式是否正确
4. **统计信息**: 验证统计计算是否准确

### 功能验证
1. **查询条件**: 验证各种查询条件的正确性
2. **分页功能**: 测试不同页码和页面大小的表现
3. **排序功能**: 验证按不同字段排序的结果
4. **时间范围**: 测试时间范围查询的准确性

### 性能验证
1. **响应时间**: 检查查询响应时间是否合理
2. **大数据量**: 测试大数据量查询的性能
3. **并发查询**: 验证并发访问的稳定性

## 错误场景测试

### 1. 无效参数测试
```bash
# 无效的页码
curl -X GET "http://localhost:8080/web/open/gpu-tasks/query?page=0"

# 过大的页面大小
curl -X GET "http://localhost:8080/web/open/gpu-tasks/query?pageSize=10000"

# 无效的时间格式
curl -X GET "http://localhost:8080/web/open/gpu-tasks/query?startTime=invalid"
```

### 2. 不存在的查询条件
```bash
# 不存在的用户
curl -X GET "http://localhost:8080/web/open/gpu-tasks/query?userName=nonexistent"

# 不存在的项目
curl -X GET "http://localhost:8080/web/open/gpu-tasks/query?projectName=nonexistent"
```

## 日志监控

在测试过程中，关注应用日志的输出：
- 查询请求日志
- 查询执行时间
- 错误和异常信息
- 统计计算日志

## 测试报告

测试完成后，记录以下信息：
1. 测试用例执行结果
2. 发现的问题和异常
3. 性能指标数据
4. 改进建议

## 注意事项

1. **测试数据**: 确保数据库中有足够的测试数据
2. **环境隔离**: 测试环境应与生产环境隔离
3. **数据安全**: 测试过程中注意数据安全和隐私保护
4. **性能基准**: 建立性能基准，便于后续对比