# Group Center

## Group Center 系列项目

- [Group Center Backend(本项目)](https://github.com/a645162/group-center)
- [Group Center Client](https://github.com/a645162/group-center-client)
- [Group Center Dashboard](https://github.com/a645162/group-center-dashboard)
- [nvi-notify](https://github.com/a645162/nvi-notify)
- [web-gpu-dashboard](https://github.com/a645162/web-gpu-dashboard)

### Group Center Backend

Group Center的核心项目，基于`Spring Boot`的后端，支持GPU任务信息记录，以及消息推送。

### Group Center Client

注意：需要配合`group-center`使用

### Group Center Dashboard

注意：需要配合`group-center`以及`nvi-notify`使用

### nvi-notify

NVIDIA GPU服务器监控通知工具，主要用于监控NVIDIA GPU服务器的GPU使用情况，并通过企业微信(WeCom)进行通知。

支持CPU服务器(无GPU)以及NVIDIA GPU服务器。

支持通过`group-center`/`group-center-dashboard`/`web-gpu-dashboard`拓展功能。

注意:本项目依赖于`group-center-client`，`requirements.txt`中已经给出具体版本需求。

### web-gpu-dashboard

旧版GPU看板，主要支持查看多台服务器上的GPU状况，以及任务情况。

注意：需要配合`nvi-notify`使用

## Group Center Backend

### 支持功能

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
