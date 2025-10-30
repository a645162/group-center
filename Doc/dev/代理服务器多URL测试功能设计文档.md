# 代理服务器多URL测试功能设计文档

## 功能概述

本功能扩展了原有的代理服务器健康检查系统，支持为每个代理服务器配置多个测试URL，并提供详细的URL测试结果。

## 主要改进

### 1. 配置结构扩展

#### 原有配置结构
```yaml
testConfig:
  testUrl: "https://www.google.com"
  directTestUrl: "https://www.google.com"
  timeout: 10
```

#### 新配置结构
```yaml
testConfig:
  testUrls:
    - url: "https://www.google.com"
      name: "Google测试"
      nameEng: "google-test"
      enable: true
      expectedStatusCode: 200
    - url: "https://api.wandb.ai"
      name: "Wandb测试"
      nameEng: "wandb-test"
      enable: true
      expectedStatusCode: 200
  directTestUrl: "https://www.google.com"
  timeout: 10
```

### 2. 向后兼容性

系统自动兼容旧配置格式，如果配置中只有 `testUrl` 字段，会自动转换为新的 `testUrls` 格式。

### 3. 测试策略

- **多URL测试**：每个代理服务器可以配置多个测试URL
- **智能判断**：只要有一个URL测试成功，就认为代理服务器可用
- **详细结果**：记录每个URL的测试结果，包括响应时间、状态码、错误信息

## API接口扩展

### 代理健康检查接口

#### 请求
```
GET /api/proxy/health-check
```

#### 响应
```json
{
  "success": true,
  "message": "Proxy health check completed",
  "data": {
    "totalProxies": 3,
    "availableProxies": 2,
    "availabilityRate": 66.67,
    "averageResponseTime": 150,
    "lastCheckTime": 1698654321000,
    "isConfigEnabled": true,
    "proxyResults": [
      {
        "proxyName": "http-proxy-test",
        "isAvailable": true,
        "responseTime": 120,
        "urlTestResults": [
          {
            "url": "https://www.google.com",
            "name": "Google测试",
            "nameEng": "google-test",
            "isSuccess": true,
            "responseTime": 120,
            "statusCode": 200,
            "error": null,
            "testTime": 1698654321000
          },
          {
            "url": "https://api.wandb.ai",
            "name": "Wandb测试",
            "nameEng": "wandb-test",
            "isSuccess": false,
            "responseTime": null,
            "statusCode": null,
            "error": "Connection timeout",
            "testTime": 1698654321000
          }
        ]
      }
    ]
  }
}
```

## 配置示例

### 完整配置示例

```yaml
# Config/Proxy/example.yaml
enable: true
proxyTests:
  - name: "HTTP代理测试"
    nameEng: "http-proxy-test"
    type: "HTTP"
    host: "proxy.example.com"
    port: 8080
    enable: true
    testConfig:
      testUrls:
        - url: "https://www.google.com"
          name: "Google测试"
          nameEng: "google-test"
          enable: true
          expectedStatusCode: 200
        - url: "https://api.wandb.ai"
          name: "Wandb测试"
          nameEng: "wandb-test"
          enable: true
          expectedStatusCode: 200
        - url: "https://www.github.com"
          name: "GitHub测试"
          nameEng: "github-test"
          enable: false
          expectedStatusCode: 200
      directTestUrl: "https://www.google.com"
      timeout: 10
```

## 技术实现

### 1. 数据结构

- **TestUrlConfig**: 单个测试URL的配置
- **UrlTestResult**: 单个URL的测试结果
- **ProxyTestServer**: 代理服务器配置（扩展了testUrls字段）

### 2. 核心服务

- **ProxyHealthCheckService**: 健康检查服务，支持多URL测试
- **ProxyConfigLoader**: 配置加载器，支持新旧配置格式兼容
- **ProxyHealthCheckScheduler**: 定时任务调度器

### 3. 测试策略

```kotlin
// 只要有一个URL测试成功就认为代理可用
var overallSuccess = false
for (testUrlConfig in enabledTestUrls) {
    val result = testUrlWithProxy(proxy, testUrlConfig)
    if (result.isSuccess) {
        overallSuccess = true
        break  // 可选：提前结束测试
    }
}
```

## 使用说明

### 1. 配置多个测试URL

在代理配置文件中，使用 `testUrls` 数组配置多个测试目标：

```yaml
testConfig:
  testUrls:
    - url: "https://www.google.com"
      name: "Google测试"
      nameEng: "google-test"
      enable: true
      expectedStatusCode: 200
    - url: "https://api.wandb.ai"
      name: "Wandb测试"
      nameEng: "wandb-test"
      enable: true
      expectedStatusCode: 200
```

### 2. 查看详细测试结果

通过API接口可以获取每个代理服务器的详细URL测试结果：

```bash
curl http://localhost:8080/api/proxy/health-check
```

### 3. 监控和日志

系统会记录详细的测试日志：

```
Proxy http-proxy-test URL test successful: google-test, response time: 120ms
Proxy http-proxy-test URL test failed: wandb-test, status code: 503 (expected: 200)
```

## 优势

1. **更全面的测试覆盖**：可以测试代理服务器对不同网站的访问能力
2. **更准确的可用性判断**：避免因单个网站问题误判代理不可用
3. **更详细的诊断信息**：提供每个URL的测试结果，便于问题排查
4. **向后兼容**：自动支持旧配置格式，无需手动迁移

## 注意事项

1. 建议为每个代理服务器配置2-3个不同的测试URL
2. 测试URL应选择稳定可靠的网站
3. 可以根据需要启用或禁用特定的测试URL
4. 系统会自动选择响应最快的成功结果作为代理的响应时间