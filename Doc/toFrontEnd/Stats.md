
# 统计系统接口文档

## 概述

本系统提供完整的GPU使用统计功能，包括用户统计、GPU统计、服务器统计、项目统计以及各种时间周期的报告。所有接口都支持缓存机制，提高响应速度。

## 基础信息

### 基础URL
```
/web/dashboard/statistics
```

### 时间周期参数
所有支持时间周期的接口都使用以下枚举值：

| 枚举值 | 中文描述 | 时间范围 |
|--------|----------|----------|
| `ONE_DAY` | 一天 | 24小时 |
| `ONE_WEEK` | 一周 | 7天 |
| `ONE_MONTH` | 一个月 | 30天 |
| `SIX_MONTH` | 六个月 | 180天 |
| `ONE_YEAR` | 一年 | 365天 |
| `THREE_YEAR` | 三年 | 1095天 |
| `ALL` | 全部时间 | 所有数据 |

### 通用响应格式
所有接口返回统一的响应格式：
```json
{
  "result": "具体数据对象",
  "success": true,
  "message": "操作结果消息"
}
```

## 统计接口

### 1. 用户统计

**接口**: `GET /web/dashboard/statistics/users`

**描述**: 获取用户统计数据，包括活跃用户、任务数量、使用模式等

**参数**:
- `timePeriod` (可选): 时间周期，默认 `ONE_WEEK`

**示例调用**:
```bash
GET /web/dashboard/statistics/users?timePeriod=ONE_WEEK
```

**响应示例**:
```json
{
  "result": [
    {
      "userName": "user1",
      "totalTasks": 15,
      "totalRuntime": 86400,
      "averageRuntime": 5760.0,
      "favoriteGpu": "RTX 4090",
      "favoriteProject": "AI Training",
      "formattedAverageRuntime": 5760.0
    },
    {
      "userName": "user2",
      "totalTasks": 8,
      "totalRuntime": 43200,
      "averageRuntime": 5400.0,
      "favoriteGpu": "A100",
      "favoriteProject": "Research",
      "formattedAverageRuntime": 5400.0
    }
  ],
  "success": true,
  "message": "Success"
}
```

### 2. GPU统计

**接口**: `GET /web/dashboard/statistics/gpus`

**描述**: 获取GPU使用统计数据，包括利用率、任务分布、性能指标等

**参数**:
- `timePeriod` (可选): 时间周期，默认 `ONE_WEEK`

**示例调用**:
```bash
GET /web/dashboard/statistics/gpus?timePeriod=ONE_MONTH
```

**响应示例**:
```json
{
  "result": [
    {
      "gpuName": "RTX 4090",
      "serverName": "server1",
      "totalUsageCount": 25,
      "totalRuntime": 172800,
      "averageUsagePercent": 85.5,
      "averageMemoryUsage": 78.2,
      "totalMemoryUsage": 512.0,
      "formattedAverageUsagePercent": 85.5,
      "formattedAverageMemoryUsage": 78.2,
      "formattedTotalMemoryUsage": 512.0
    },
    {
      "gpuName": "A100",
      "serverName": "server2",
      "totalUsageCount": 18,
      "totalRuntime": 129600,
      "averageUsagePercent": 92.1,
      "averageMemoryUsage": 85.7,
      "totalMemoryUsage": 640.0,
      "formattedAverageUsagePercent": 92.1,
      "formattedAverageMemoryUsage": 85.7,
      "formattedTotalMemoryUsage": 640.0
    }
  ],
  "success": true,
  "message": "Success"
}
```

### 3. 服务器统计

**接口**: `GET /web/dashboard/statistics/servers`

**描述**: 获取服务器统计数据，包括机器状态、可用性、资源使用情况等

**参数**:
- `timePeriod` (可选): 时间周期，默认 `ONE_WEEK`

**示例调用**:
```bash
GET /web/dashboard/statistics/servers?timePeriod=ONE_WEEK
```

**响应示例**:
```json
{
  "result": [
    {
      "serverName": "server1",
      "totalTasks": 35,
      "totalRuntime": 259200,
      "activeUsers": ["user1", "user2", "user3"],
      "gpuUtilization": 78.5,
      "activeUsersCount": 3,
      "formattedGpuUtilization": 78.5
    },
    {
      "serverName": "server2",
      "totalTasks": 28,
      "totalRuntime": 201600,
      "activeUsers": ["user2", "user4"],
      "gpuUtilization": 85.2,
      "activeUsersCount": 2,
      "formattedGpuUtilization": 85.2
    }
  ],
  "success": true,
  "message": "Success"
}
```

### 4. 项目统计

**接口**: `GET /web/dashboard/statistics/projects`

**描述**: 获取项目统计数据，包括项目数量、任务分布、使用模式等

**参数**:
- `timePeriod` (可选): 时间周期，默认 `ONE_WEEK`

**示例调用**:
```bash
GET /web/dashboard/statistics/projects?timePeriod=ONE_MONTH
```

**响应示例**:
```json
{
  "result": [
    {
      "projectName": "AI Training",
      "totalRuntime": 302400,
      "totalTasks": 42,
      "activeUsers": ["user1", "user2", "user3", "user5"],
      "averageRuntime": 7200.0,
      "activeUsersCount": 4,
      "formattedAverageRuntime": 7200.0
    },
    {
      "projectName": "Research",
      "totalRuntime": 216000,
      "totalTasks": 30,
      "activeUsers": ["user2", "user4"],
      "averageRuntime": 7200.0,
      "activeUsersCount": 2,
      "formattedAverageRuntime": 7200.0
    }
  ],
  "success": true,
  "message": "Success"
}
```

### 5. 时间趋势统计

**接口**: `GET /web/dashboard/statistics/time-trend`

**描述**: 获取基于时间的趋势统计数据，显示随时间变化的使用模式

**参数**:
- `timePeriod` (可选): 时间周期，默认 `ONE_WEEK`

**示例调用**:
```bash
GET /web/dashboard/statistics/time-trend?timePeriod=ONE_WEEK
```

**响应示例**:
```json
{
  "result": {
    "period": "ONE_WEEK",
    "dailyStats": [
      {
        "date": "2024-01-01",
        "totalTasks": 8,
        "totalRuntime": 28800,
        "activeUsers": ["user1", "user2"],
        "peakGpuUsage": 85.5,
        "activeUsersCount": 2,
        "formattedPeakGpuUsage": 85.5
      },
      {
        "date": "2024-01-02",
        "totalTasks": 12,
        "totalRuntime": 43200,
        "activeUsers": ["user1", "user2", "user3"],
        "peakGpuUsage": 92.1,
        "activeUsersCount": 3,
        "formattedPeakGpuUsage": 92.1
      }
    ],
    "totalTasks": 72,
    "totalRuntime": 259200,
    "totalUsers": 5,
    "averageDailyTasks": 10,
    "averageDailyRuntime": 37028
  },
  "success": true,
  "message": "Success"
}
```

## 报告接口

### 6. 24小时报告

**接口**: `GET /web/dashboard/statistics/reports/24hour`

**描述**: 获取过去24小时的综合使用报告，包括任务数量、用户活动、资源利用率等

**示例调用**:
```bash
GET /web/dashboard/statistics/reports/24hour
```

**响应示例**:
```json
{
  "result": {
    "reportType": "CUSTOM",
    "title": "24小时报告",
    "periodStartDate": "2024-01-02",
    "periodEndDate": "2024-01-02",
    "startTime": "2024-01-02T15:00:00",
    "endTime": "2024-01-03T15:00:00",
    "actualTaskStartTime": "2024-01-02T15:30:00",
    "actualTaskEndTime": "2024-01-03T14:45:00",
    "totalTasks": 18,
    "totalRuntime": 64800,
    "activeUsers": 4,
    "topUsers": [
      {
        "userName": "user1",
        "totalTasks": 6,
        "totalRuntime": 21600,
        "averageRuntime": 3600.0,
        "favoriteGpu": "RTX 4090",
        "favoriteProject": "AI Training",
        "formattedAverageRuntime": 3600.0
      }
    ],
    "topGpus": [
      {
        "gpuName": "RTX 4090",
        "serverName": "server1",
        "totalUsageCount": 8,
        "totalRuntime": 28800,
        "averageUsagePercent": 87.2,
        "averageMemoryUsage": 79.5,
        "totalMemoryUsage": 256.0,
        "formattedAverageUsagePercent": 87.2,
        "formattedAverageMemoryUsage": 79.5,
        "formattedTotalMemoryUsage": 256.0
      }
    ],
    "topProjects": [
      {
        "projectName": "AI Training",
        "totalRuntime": 43200,
        "totalTasks": 12,
        "activeUsers": ["user1", "user2", "user3"],
        "averageRuntime": 3600.0,
        "activeUsersCount": 3,
        "formattedAverageRuntime": 3600.0
      }
    ],
    "sleepAnalysis": null,
    "refreshTime": "2024-01-03T15:00:05"
  },
  "success": true,
  "message": "Success"
}
```

### 7. 48小时报告

**接口**: `GET /web/dashboard/statistics/reports/48hour`

**描述**: 获取过去48小时的扩展趋势分析报告

**示例调用**:
```bash
GET /web/dashboard/statistics/reports/48hour
```

**响应格式**: 与24小时报告相同，但时间范围更长

### 8. 72小时报告

**接口**: `GET /web/dashboard/statistics/reports/72hour`

**描述**: 获取过去72小时的详细趋势分析和模式报告

**示例调用**:
```bash
GET /web/dashboard/statistics/reports/72hour
```

**响应格式**: 与24小时报告相同，但时间范围更长

### 9. 今日日报

**接口**: `GET /web/dashboard/statistics/reports/today`

**描述**: 获取今日日报（从00:00到23:59）

**示例调用**:
```bash
GET /web/dashboard/statistics/reports/today
```

**响应示例**:
```json
{
  "result": {
    "reportType": "TODAY",
    "title": "GPU使用日报 - 今日",
    "periodStartDate": "2024-01-03",
    "periodEndDate": "2024-01-03",
    "startTime": "2024-01-03T00:00:00",
    "endTime": "2024-01-04T00:00:00",
    "actualTaskStartTime": "2024-01-03T08:30:00",
    "actualTaskEndTime": "2024-01-03T22:45:00",
    "totalTasks": 12,
    "totalRuntime": 43200,
    "activeUsers": 3,
    "topUsers": [...],
    "topGpus": [...],
    "topProjects": [...],
    "sleepAnalysis": null,
    "refreshTime": "2024-01-03T15:00:05"
  },
  "success": true,
  "message": "Success"
}
```

### 10. 昨日日报

**接口**: `GET /web/dashboard/statistics/reports/yesterday`

**描述**: 获取昨日日报（昨天0:00到今天0:00）

**示例调用**:
```bash
GET /web/dashboard/statistics/reports/yesterday
```

**响应格式**: 与今日日报相同，但时间范围是昨天

### 11. 周报

**接口**: `GET /web/dashboard/statistics/reports/weekly`

**描述**: 获取上周的周报

**示例调用**:
```bash
GET /web/dashboard/statistics/reports/weekly
```

**响应示例**:
```json
{
  "result": {
    "reportType": "WEEKLY",
    "title": "GPU使用周报 - 上周",
    "periodStartDate": "2023-12-25",
    "periodEndDate": "2023-12-31",
    "startTime": "2023-12-25T00:00:00",
    "endTime": "2023-12-31T23:59:59",
    "actualTaskStartTime": "2023-12-25T08:15:00",
    "actualTaskEndTime": "2023-12-31T23:30:00",
    "totalTasks": 84,
    "totalRuntime": 302400,
    "activeUsers": 6,
    "topUsers": [...], // 最多10个用户
    "topGpus": [...], // 最多10个GPU
    "topProjects": [...], // 最多10个项目
    "sleepAnalysis": null,
    "refreshTime": "2024-01-01T00:05:00"
  },
  "success": true,
  "message": "Success"
}
```

### 12. 月报

**接口**: `GET /web/dashboard/statistics/reports/monthly`

**描述**: 获取上月的月报

**示例调用**:
```bash
GET /web/dashboard/statistics/reports/monthly
```

**响应示例**:
```json
{
  "result": {
    "reportType": "MONTHLY",
    "title": "GPU使用月报 - 上月",
    "periodStartDate": "2023-12-01",
    "periodEndDate": "2023-12-31",
    "startTime": "2023-12-01T00:00:00",
    "endTime": "2023-12-31T23:59:59",
    "actualTaskStartTime": "2023-12-01T08:00:00",
    "actualTaskEndTime": "2023-12-31T23:45:00",
    "totalTasks": 360,
    "totalRuntime": 1296000,
    "activeUsers": 8,
    "topUsers": [...], // 最多15个用户
    "topGpus": [...], // 最多15个GPU
    "topProjects": [...], // 最多10个项目
    "sleepAnalysis": null,
    "refreshTime": "2024-01-01T00:10:00"
  },
  "success": true,
  "message": "Success"
}
```

### 13. 年报

**接口**: `GET /web/dashboard/statistics/reports/yearly`

**描述**: 获取去年的年报

**示例调用**:
```bash
GET /web/dashboard/statistics/reports/yearly
```

**响应示例**:
```json
{
  "result": {
    "reportType": "YEARLY",
    "title": "GPU使用年报 - 去年",
    "periodStartDate": "2023-01-01",
    "periodEndDate": "2023-12-31",
    "startTime": "2023-01-01T00:00:00",
    "endTime": "2023-12-31T23:59:59",
    "actualTaskStartTime": "2023-01-01T09:00:00",
    "actualTaskEndTime": "2023-12-31T22:30:00",
    "totalTasks": 4320,
    "totalRuntime": 15552000,
    "activeUsers": 12,
    "topUsers": [...], // 最多20个用户
    "topGpus": [...], // 最多20个GPU
    "topProjects": [...], // 最多15个项目
    "sleepAnalysis": null,
    "refreshTime": "2024-01-01T00:15:00"
  },
  "success": true,
  "message": "Success"
}
```

## 高级功能接口

### 14. 自定义时间段统计

**接口**: `GET /web/dashboard/statistics/custom`

**描述**: 获取自定义时间段的统计报告

**参数**:
- `startTime` (必需): 开始时间，格式 `yyyy-MM-ddTHH:mm:ss`
- `endTime` (必需): 结束时间，格式 `yyyy-MM-ddTHH:mm:ss`

**示例调用**:
```bash
GET /web/dashboard/statistics/custom?startTime=2024-01-01T00:00:00&endTime=2024-01-07T23:59:59
```

**响应格式**: 与报告接口相同

### 15. 作息时间分析

**接口**: `GET /web/dashboard/statistics/sleep-analysis`

**描述**: 获取用户作息时间分析，识别熬夜和早起模式

**参数**:
- `timePeriod` (可选): 时间周期，默认 `ONE_WEEK`

**示例调用**:
```bash
GET /web/dashboard/statistics/sleep-analysis?timePeriod=ONE_WEEK
```

**响应示例**:
```json
{
  "result": {
    "lateNightTasks": [
      {
        "taskId": 12345,
        "taskUser": "user1",
        "taskGpuName": "RTX 4090",
        "serverName": "server1",
        "taskStartTime": 1704060000,
        "taskFinishTime": 1704067200,
        "taskRunningTimeInSeconds": 7200,
        "projectName": "AI Training",
        "gpuUsagePercent": 85.5,
        "gpuMemoryPercent": 78.2,
        "taskGpuMemoryGb": 16.0
      }
    ],
    "earlyMorningTasks": [
      {
        "taskId": 12346,
        "taskUser": "user2",
        "taskGpuName": "A100",
        "serverName": "server2",
        "taskStartTime": 1704006000,
        "taskFinishTime": 1704013200,
        "taskRunningTimeInSeconds": 7200,
        "projectName": "Research",
        "gpuUsagePercent": 92.1,
        "gpuMemoryPercent": 85.7,
        "taskGpuMemoryGb": 40.0
      }
    ],
    "lateNightChampion": {
      "taskId": 12347,
      "taskUser": "user3",
      "taskGpuName": "RTX 4090",
      "serverName": "server1",
      "taskStartTime": 1704069000,
      "taskFinishTime": 1704076200,
      "taskRunningTimeInSeconds": 7200,
      "projectName": "AI Training",
      "gpuUsagePercent": 88.3,
      "gpuMemoryPercent": 81.5,
      "taskGpuMemoryGb": 16.0
    },
    "earlyMorningChampion": {
      "taskId": 12348,
      "taskUser": "user4",
      "taskGpuName": "A100",
      "serverName": "server2",
      "taskStartTime": 1704002400,
      "taskFinishTime": 1704009600,
      "taskRunningTimeInSeconds": 7200,
      "projectName": "Research",
      "gpuUsagePercent": 90.7,
      "gpuMemoryPercent": 83.9,
      "taskGpuMemoryGb": 40.0
    },
    "totalLateNightTasks": 8,
    "totalEarlyMorningTasks": 6,
    "lateNightUsers": ["user1", "user3", "user5"],
    "earlyMorningUsers": ["user2", "user4", "user6"],
    "refreshTime": "2024-01-03T15:00:05",
    "totalLateNightUsers": 3,
    "totalEarlyMorningUsers": 3
  },
  "success": true,
  "message": "Success"
}
```

## 缓存管理接口

### 16. 强制更新统计缓存

**接口**: `POST /web/dashboard/statistics/cache/update`

**描述**: 强制更新所有统计缓存，重新计算所有统计数据

**示例调用**:
```bash
POST /web/dashboard/statistics/cache/update
```

**响应示例**:
```json
{
  "result": "Statistics cache updated successfully",
  "success": true,
  "message": "Success"
}
```

### 17. 清除统计缓存

**接口**: `POST /web/dashboard/statistics/cache/clear`

**描述**: 清除所有统计缓存

**示例调用**:
```bash
POST /web/dashboard/statistics/cache/clear
```

**响应示例**:
```json
{
  "result": "Statistics cache cleared successfully",
  "success": true,
  "message": "Success"
}
```

## 数据结构说明

### Report 数据结构
```json
{
  "reportType": "TODAY | YESTERDAY | WEEKLY | MONTHLY | YEARLY | CUSTOM",
  "title": "报告标题",
  "periodStartDate": "2024-01-01",
  "periodEndDate": "2024-01-01",
  "startTime": "2024-01-01T00:00:00",
  "endTime": "2024-01-02T00:00:00",
  "actualTaskStartTime": "2024-01-01T08:30:00",
  "actualTaskEndTime": "2024-01-01T22:45:00",
  "totalTasks": 12,
  "totalRuntime": 43200,
  "activeUsers": 3,
  "topUsers": [...],
  "topGpus": [...],
  "topProjects": [...],
  "sleepAnalysis": null,
  "refreshTime": "2024-01-01T15:00:05"
}
```

### UserStatistics 数据结构
```json
{
  "userName": "user1",
  "totalTasks": 15,
  "totalRuntime": 86400,
  "averageRuntime": 5760.0,
  "favoriteGpu": "RTX 4090",
  "favoriteProject": "AI Training",
  "formattedAverageRuntime": 5760.0
}
```

### GpuStatistics 数据结构
```json
{
  "gpuName": "RTX 4090",
  "serverName": "server1",
  "totalUsageCount": 25,
  "totalRuntime": 172800,
  "averageUsagePercent": 85.5,
  "averageMemoryUsage": 78.2,
  "totalMemoryUsage": 512.0,
  "formattedAverageUsagePercent": 85.5,
  "formattedAverageMemoryUsage": 78.2,
  "formattedTotalMemoryUsage": 512.0
}
```

### SleepAnalysis 数据结构
```json
{
  "lateNightTasks": [...],
  "earlyMorningTasks": [...],
  "lateNightChampion": {...},
  "earlyMorningChampion": {...},
  "totalLateNightTasks": 8,
  "totalEarlyMorningTasks": 6,
  "lateNightUsers": ["user1", "user3", "user5"],
  "earlyMorningUsers": ["user2", "user4", "user6"],
  "refreshTime": "2024-01-03T15:00:05",
  "totalLateNightUsers": 3,
  "totalEarlyMorningUsers": 3
}
```

## 使用建议

### 1. 缓存策略
- 所有统计接口都支持缓存，提高响应速度
- 缓存会在数据更新时自动刷新
- 可以使用缓存管理接口手动更新或清除缓存

### 2. 时间周期选择
- **实时监控**: 使用 `ONE_DAY` 或小时报告
- **趋势分析**: 使用 `ONE_WEEK` 或 `ONE_MONTH`
- **长期统计**: 使用 `ONE_YEAR` 或 `ALL`

### 3. 性能优化
- 对于频繁访问的数据，建议前端实现本地缓存
- 批量获取数据时，使用合适的时间周期避免数据量过大
- 使用缓存管理接口在需要时强制更新数据

### 4. 错误处理
- 所有接口返回统一的响应格式
- 检查 `success` 字段判断操作是否成功
- 查看 `message` 字段获取详细错误信息

## 前端集成示例

### React 组件示例
```jsx
import React, { useState, useEffect } from 'react';

const StatisticsDashboard = () => {
  const [userStats, setUserStats] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchUserStatistics();
  }, []);

  const fetchUserStatistics = async () => {
    try {
      const response = await fetch('/web/dashboard/statistics/users?timePeriod=ONE_WEEK');
      const data = await response.json();
      if (data.success) {
        setUserStats(data.result);
      }
    } catch (error) {
      console.error('Failed to fetch user statistics:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h2>用户统计</h2>
      <table>
        <thead>
          <tr>
            <th>用户名</th>
            <th>任务数</th>
            <th>总运行时间</th>
            <th>平均运行时间</th>
            <th>常用GPU</th>
            <th>常用项目</th>
          </tr>
        </thead>
        <tbody>
          {userStats.map(user => (
            <tr key={user.userName}>
              <td>{user.userName}</td>
              <td>{user.totalTasks}</td>
              <td>{formatRuntime(user.totalRuntime)}</td>
              <td>{formatRuntime(user.averageRuntime)}</td>
              <td>{user.favoriteGpu}</td>
              <td>{user.favoriteProject}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

const formatRuntime = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  return `${hours}h ${minutes}m`;
};

export default StatisticsDashboard;
```

### 报告显示组件示例
```jsx
import React, { useState, useEffect } from 'react';

const ReportViewer = () => {
  const [report, setReport] = useState(null);
  const [reportType, setReportType] = useState('today');

  useEffect(() => {
    fetchReport();
  }, [reportType]);

  const fetchReport = async () => {
    try {
      const endpoint = `/web/dashboard/statistics/reports/${reportType}`;
      const response = await fetch(endpoint);
      const data = await response.json();
      if (data.success) {
        setReport(data.result);
      }
    } catch (error) {
      console.error('Failed to fetch report:', error);
    }
  };

  if (!report) return <div>Loading report...</div>;

  return (
    <div>
      <div>
        <button onClick={() => setReportType('today')}>今日</button>
        <button onClick={() => setReportType('yesterday')}>昨日</button>
        <button onClick={() => setReportType('weekly')}>周报</button>
        <button onClick={() => setReportType('monthly')}>月报</button>
      </div>
      
      <h2>{report.title}</h2>
      <p>时间范围: {report.startTime} - {report.endTime}</p>
      <p>总任务数: {report.totalTasks}</p>
      <p>总运行时间: {formatRuntime(report.totalRuntime)}</p>
      <p>活跃用户: {report.activeUsers}</p>
      
      <h3>Top 用户</h3>
      <ul>
        {report.topUsers.map(user => (
          <li key={user.userName}>
            {user.userName}: {user.totalTasks} 任务, {formatRuntime(user.totalRuntime)}
          </li>
        ))}
      </ul>
    </div>
  );
};
```

这个文档提供了完整的统计系统接口说明，包括详细的接口描述、参数说明、响应示例和使用建议，可以帮助前端开发者快速集成统计功能。