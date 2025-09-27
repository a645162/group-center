# Group Center

## Group Center 系列项目

- [Group Center Backend(本项目)](https://github.com/a645162/group-center)
- [Group Center Client](https://github.com/a645162/group-center-client)
- [Group Center Dashboard](https://github.com/a645162/group-center-dashboard)
- [Group Center Agent(nvi-notify)](https://github.com/a645162/nvi-notify)

## Group Center Backend

### 语言及构建工具

- Java
- Kotlin
- Gradle(Kotlin DSL)

### IDE及开发辅助工具

- Alibaba DragonWell JDK 21
- Jetbrains Intellij IDEA
- Jetbrains DataGrip
- PDManer

### 框架

- Spring Boot

### 数据库

- Alibaba Druid
- MyBatis
- MyBatis Plus

### 支持的数据库

- MySQL
- Alibaba OceanBase

### 认证与加密

- JWT(使用SM3算法自己实现的JWT)
- Tencent Kona Crypto(国密SM3/SM4算法)

### 消息推送

- 企业微信(WeCom)
- 飞书(Lark)

### 报表(TODO)

## 新功能：统计分析与Bot推送

### 数据存储
- **位置**: MySQL数据库 `gpu_task_info` 表
- **缓存机制**: 内存缓存，1小时自动更新
- **统计维度**: 用户、GPU、时间周期

### 统计功能
支持以下统计指标：
- 用户统计：任务启动次数、运行时间、最常用GPU
- GPU统计：使用频率、使用时间、平均使用率
- 时间统计：日报、周报、月报、年报

### RESTful API接口

#### 统计接口
```
GET /web/dashboard/stats/users?timePeriod=ONE_WEEK
GET /web/dashboard/stats/gpus?timePeriod=ONE_WEEK
GET /web/dashboard/reports/daily
GET /web/dashboard/reports/weekly
GET /web/dashboard/reports/monthly
GET /web/dashboard/reports/yearly
POST /web/dashboard/cache/clear
```

#### Bot推送接口
```
GET /web/bot/groups
POST /web/bot/groups
POST /web/bot/push/{type}
POST /web/bot/push/group/{groupName}
POST /web/bot/push/alarm
POST /web/bot/push/daily
POST /web/bot/push/weekly
POST /web/bot/push/monthly
POST /web/bot/push/yearly
```

### Bot群配置
在 `src/main/resources/bot-groups.yaml` 中配置：
```yaml
bot:
  groups:
    - name: "报警群"
      type: "alarm"
      weComGroupBotKey: "your-wecom-key"
      larkGroupBotId: "your-lark-id"
      larkGroupBotKey: "your-lark-key"
      enable: true
```

### 自动定时任务
- **日报**: 每天早上8点自动生成并推送
- **周报**: 每周一早上9点自动推送
- **月报**: 每月1号早上10点自动推送
- **年报**: 每年1月1号早上11点自动推送
- **缓存更新**: 每小时自动更新统计缓存

### 使用示例

#### 获取用户统计
```bash
curl "http://localhost:8080/web/dashboard/stats/users?timePeriod=ONE_WEEK"
```

#### 推送报警消息
```bash
curl -X POST "http://localhost:8080/web/bot/push/alarm?title=系统报警&content=GPU使用率过高"
```

#### 配置Bot群
```bash
curl -X POST "http://localhost:8080/web/bot/groups" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "日报群",
    "type": "daily",
    "weComGroupBotKey": "your-key",
    "enable": true
  }'
```

画个饼，初步计划集成JimuReport积木报表。

#### JimuReport积木报表

https://github.com/jeecgboot/jimureport

### 旧版项目

- [web-gpu-dashboard](https://github.com/a645162/web-gpu-dashboard)

### Group Center 系列项目介绍

### Group Center Backend

Group Center的核心项目，基于`Spring Boot`的后端，支持GPU任务信息记录，以及消息推送。

#### 支持的功能

- GPU信息记录
- WebHook消息推送
- 用户信息分发
- 配置文件分发
- SSH密钥上传与下载

#### 主要技术栈

- Kotlin
- Spring Boot
- MyBatis Plus
- MySQL
- Gradle

### Group Center Client

注意：需要配合`group-center`使用

#### 支持的功能

- 用户自定义消息推送
- SSH密钥备份与恢复
- Linux用户管理(需要服务器配置用户)
- 与`group-center`通信(用于二次开发)

#### 主要技术栈

- Python 3
- requests

### Group Center Dashboard

注意：需要配合`group-center`以及`nvi-notify`使用

#### 支持的功能

- 网址导航
- GPU看板
  - GPU使用情况
  - GPU任务情况
- 硬盘情况看板

#### 主要技术栈

- TypeScript
- React
- Ant Design
- Zustand
- UmiJS
- Axios

### nvi-notify(Group Center Agent)

NVIDIA GPU服务器监控通知工具，主要用于监控NVIDIA GPU服务器的GPU使用情况，并通过企业微信(WeCom)进行通知。

支持CPU服务器(无GPU)以及NVIDIA GPU服务器。

支持通过`group-center`/`group-center-dashboard`/`web-gpu-dashboard`拓展功能。

注意:本项目依赖于`group-center-client`，`requirements.txt`中已经给出具体版本需求。

#### 支持的功能

- GPU使用情况监控
- GPU任务情况监控
- GPU信息推送至`group-center`
- RESTful API

#### 主要技术栈

- Python 3
- flask

### web-gpu-dashboard

旧版GPU看板，主要支持查看多台服务器上的GPU状况，以及任务情况。

注意：需要配合`nvi-notify`使用

#### 支持的功能

- 查看GPU信息
- 查看GPU任务信息
- 查看内存信息

#### 主要技术栈

- TypeScript
- VUE3
- Element Plus
- Pinia
- Axios
