# 代理服务器URL测试结果修复说明

## 问题描述

在之前的API响应中，`urlTestResults` 字段始终返回空数组 `[]`，无法显示每个URL的详细测试结果。

## 问题分析

1. **URL测试结果未存储**：在健康检查过程中，虽然生成了详细的URL测试结果，但这些结果没有被保存到代理状态中
2. **API响应使用空列表**：`ProxyStatusDetails` 类中的 `urlTestResults` 字段被硬编码为 `emptyList()`
3. **状态信息不完整**：`ProxyStatus` 类缺少存储URL测试结果的字段

## 修复方案

### 1. 扩展 ProxyStatus 类

在 [`ProxyStatus`](src/main/kotlin/com/khm/group/center/datatype/config/ProxyConfig.kt:116) 类中添加了 `lastUrlTestResults` 字段：

```kotlin
data class ProxyStatus(
    var lastCheckTime: Long? = null,
    var isAvailable: Boolean = false,
    var responseTime: Long? = null,
    var lastError: String? = null,
    var successCount: Int = 0,
    var failureCount: Int = 0,
    var lastSuccessTime: Long? = null,
    var lastUrlTestResults: List<com.khm.group.center.service.UrlTestResult> = emptyList()
)
```

### 2. 更新状态存储逻辑

修改了 [`updateProxyStatus`](src/main/kotlin/com/khm/group/center/service/ProxyHealthCheckService.kt:163) 方法，在更新代理状态时保存URL测试结果：

```kotlin
private fun updateProxyStatus(proxy: ProxyTestServer, isAvailable: Boolean, responseTime: Long?, error: String?, urlTestResults: List<UrlTestResult> = emptyList()) {
    val status = ProxyConfigManager.proxyStatusMap.getOrPut(proxy.nameEng) { ProxyStatus() }
    
    if (isAvailable) {
        status.updateSuccess(responseTime ?: 0)
    } else {
        status.updateFailure(error ?: "Unknown error")
    }
    
    // 存储URL测试结果
    status.lastUrlTestResults = urlTestResults
}
```

### 3. 更新状态详情获取

修改了 [`ProxyStatusDetails`](src/main/kotlin/com/khm/group/center/service/ProxyHealthCheckService.kt:272) 类的创建逻辑，使用存储的URL测试结果：

```kotlin
return ProxyStatusDetails(
    proxy = proxy,
    status = status,
    isConfigEnabled = ProxyConfigManager.proxyConfig.enable,
    lastCheckTime = status.lastCheckTime,
    isAvailable = status.isAvailable,
    responseTime = status.responseTime,
    successRate = status.getSuccessRate(),
    totalChecks = status.successCount + status.failureCount,
    urlTestResults = status.lastUrlTestResults  // 使用存储的URL测试结果
)
```

## 修复效果

现在API响应将包含详细的URL测试结果：

```json
{
  "servers": [
    {
      "name": "课题组-主代理",
      "nameEng": "group-main",
      "isAvailable": true,
      "urlTestResults": [
        {
          "url": "http://www.google.com",
          "name": "Google测试",
          "nameEng": "google-test",
          "isSuccess": true,
          "responseTime": 120,
          "statusCode": 200,
          "error": null,
          "testTime": 1698654321000
        },
        {
          "url": "https://wandb.ai",
          "name": "WandB 测试",
          "nameEng": "wandb-home-test",
          "isSuccess": false,
          "responseTime": null,
          "statusCode": null,
          "error": "Remote host terminated the handshake",
          "testTime": 1698654321000
        }
      ]
    }
  ]
}
```

## 验证方法

1. **编译验证**：所有代码通过Gradle构建测试
2. **功能验证**：API接口现在可以返回详细的URL测试结果
3. **配置验证**：配置文件正确加载，支持多个测试URL

## 注意事项

1. **内存使用**：URL测试结果会占用内存，但每个代理服务器最多存储最后一次的测试结果
2. **性能影响**：存储URL测试结果对性能影响很小
3. **数据持久化**：URL测试结果仅在内存中存储，应用重启后会丢失

现在用户可以查看每个代理服务器对各个测试URL的详细测试结果，包括响应时间、状态码和错误信息，便于问题诊断和性能分析。