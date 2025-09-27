# GroupPusher 使用说明

## 功能概述

GroupPusher 是一个专门用于消息推送的工具类，提供了静态方法来方便地向不同类型的群组发送消息。支持三种群组类型：

1. **报警群 (alarm)** - 用于接收系统报警和重要通知
2. **短期群 (shortterm)** - 用于接收日报和周报
3. **长期群 (longterm)** - 用于接收月报和年报

## 使用方法

### 1. 静态方法调用

```kotlin
// 推送到报警群
GroupPusher.pushToAlarmGroup("🚨 系统报警：数据库连接失败")

// 推送到短期群（日报/周报）
GroupPusher.pushToShortTermGroup("📊 今日GPU使用报告...")

// 推送到长期群（月报/年报）  
GroupPusher.pushToLongTermGroup("📈 月度统计报告...")

// 推送到指定类型的群组
GroupPusher.pushToGroup("自定义消息", "alarm") // 报警群
GroupPusher.pushToGroup("自定义消息", "shortterm") // 短期群
GroupPusher.pushToGroup("自定义消息", "longterm") // 长期群
```

### 2. 在Spring Bean中使用

```kotlin
@Service
class MyService {
    
    fun processSomething() {
        try {
            // 业务逻辑...
        } catch (e: Exception) {
            // 发生错误时推送到报警群
            GroupPusher.pushToAlarmGroup("❌ 业务处理失败: ${e.message}")
        }
    }
}
```

### 3. 在任意地方使用

```kotlin
// 在任何地方都可以直接调用
fun someUtilityFunction() {
    GroupPusher.pushToAlarmGroup("ℹ️ 系统通知：任务完成")
}
```

## 配置要求

确保 `Config/Bot/bot-groups.yaml` 文件已正确配置：

```yaml
bot:
  groups:
    - name: "报警群(管理员)"
      type: "alarm"
      weComGroupBotKey: "企业微信报警群机器人key"
      larkGroupBotId: "飞书报警群机器人ID"
      larkGroupBotKey: "飞书报警群机器人密钥"
      enable: true

    - name: "日/周报群"
      type: "shortterm"
      weComGroupBotKey: "企业微信日报群机器人key"
      larkGroupBotId: "飞书日报群机器人ID"
      larkGroupBotKey: "飞书日报群机器人密钥"
      enable: true

    - name: "年/月报群"
      type: "longterm"
      weComGroupBotKey: "企业微信月报群机器人key"
      larkGroupBotId: "飞书月报群机器人ID"
      larkGroupBotKey: "飞书月报群机器人密钥"
      enable: true
```

## 错误处理

GroupPusher 会自动处理以下错误情况：
- 配置文件不存在或格式错误
- 网络连接问题
- 机器人密钥无效
- 消息推送超时

错误信息会输出到控制台，但不会抛出异常影响主流程。

## 性能特点

- **线程安全**: 所有方法都是线程安全的
- **懒加载**: 配置只在第一次使用时加载
- **异常隔离**: 单个群组推送失败不会影响其他群组
- **静态访问**: 无需注入依赖，随处可用

## 使用场景

### 系统监控报警
```kotlin
// 监控到异常时
GroupPusher.pushToAlarmGroup("🚨 CPU使用率超过90%")

// 服务重启时
GroupPusher.pushToAlarmGroup("🔄 服务已重启完成")
```

### 定时任务报告
```kotlin
// 日报推送
GroupPusher.pushToShortTermGroup(generateDailyReport())

// 月报推送  
GroupPusher.pushToLongTermGroup(generateMonthlyReport())
```

### 业务通知
```kotlin
// 新用户注册
GroupPusher.pushToAlarmGroup("👤 新用户注册：user123")

// 重要操作完成
GroupPusher.pushToAlarmGroup("✅ 数据备份完成")
```

## 注意事项

1. 确保Bot配置文件中对应群组的 `enable` 字段为 `true`
2. 飞书机器人需要正确的权限配置
3. 消息内容长度限制：飞书约20KB，企业微信约2048字节
4. 推送失败会在控制台输出错误信息，但不会中断程序执行