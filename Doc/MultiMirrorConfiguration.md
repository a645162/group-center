# 多镜像源配置说明

## 概述

为了确保 GitHub Actions 在遇到镜像源无法访问的情况时仍能正常构建，已配置多镜像源备份策略。系统会优先尝试使用阿里云镜像，如果出现问题则自动切换到官方源。

## 配置覆盖范围

### 1. **Gradle 项目配置** (`settings.gradle.kts`)

- **主源**：阿里云镜像
  - `https://maven.aliyun.com/repository/central`
  - `https://maven.aliyun.com/repository/spring`
  - `https://maven.aliyun.com/repository/jcenter`
  - `https://maven.aliyun.com/repository/google`
  - `https://maven.aliyun.com/repository/gradle-plugin`
  - `https://maven.aliyun.com/repository/public`

- **备用源**：官方仓库
  - `mavenCentral()` - Maven 中央仓库
  - `gradlePluginPortal()` - Gradle 插件门户
  - `google()` - Google 官方仓库
  - `mavenLocal()` - 本地 Maven 仓库

**工作原理**：Gradle 会按照配置顺序尝试从各个源下载依赖。如果阿里云源不可用，会自动切换到官方源。

### 2. **GitHub Actions 工作流** (`.github/workflows/docker-publish.yml`)

在 `build-jar` 任务中包含 `Configure Maven Mirrors` 步骤时，可为 **GitHub Actions runner 主机** 创建 Maven 配置文件。

**配置位置**：`~/.m2/settings.xml`

**适用范围说明**：

- 该文件位于 runner 主机上，仅对主机环境中直接执行的 Maven 命令生效。
- 当前 `build-jar` 的实际构建链路是进入 Docker 容器后执行 `./gradlew`，因此主机上的 `~/.m2/settings.xml` 通常**不会直接被容器内构建使用**。
- 当前构建真正使用的多镜像/回退逻辑，以第 3 节中 Docker 容器内的 Gradle 初始化配置为准。
- 如果后续希望复用该 Maven 配置到容器内，需要在工作流中显式挂载或复制该配置文件。

**配置内容**（针对主机侧 Maven）：

- 主镜像：阿里云中央仓库
- 备用镜像：官方 Maven 中央仓库

### 3. **Docker 构建镜像** (`Docker/Dockerfile-Build`)

为 Docker 容器内的 Gradle 配置初始化脚本，提供多源支持。

**配置位置**：`$GRADLE_USER_HOME/init.gradle`

**配置流程**：

1. 优先尝试阿里云仓库
2. 如果阿里云不可用，依次尝试：
   - 官方 Maven 中央仓库 (`https://repo.maven.apache.org/maven2`)
   - 备用 Maven 仓库 (`https://repo1.maven.org/maven2`)
   - Gradle 提供的 Maven Central

### 4. **Gradle 属性配置** (`gradle.properties`)

- 增加网络超时时间至 60 秒（原默认值较短）
- 启用并行构建以加快构建速度

## 使用场景

### 场景 1：GitHub Actions 中文网络

当 GitHub Actions 服务器位于中文区域时：

- ✅ 阿里云镜像：快速访问
- ⏱️ 官方源：作为备用

### 场景 2：GitHub Actions 官方 DC

当 GitHub Actions 服务器位于境外时：

- ⏱️ 阿里云镜像：可能较慢
- ✅ 官方源：快速访问

### 场景 3：镜像源故障

当某个源临时不可用时：

- 系统自动切换到下一个可用源，保证构建不中断

## 故障排查

### 问题：构建仍然失败

**可能原因**：

1. 所有镜像源都无法访问（极少见）
2. 网络 DNS 解析失败
3. 防火墙阻止了所有外出连接

**解决方案**：

1. 检查 GitHub Actions 日志中的详细错误信息
2. 在 `.github/workflows/docker-publish.yml` 中添加网络诊断步骤
3. 验证所有配置的 URL 是否正确

### 问题：某些依赖下载缓慢

**优化方案**：

1. Gradle 支持缓存机制，重复构建会更快
2. 可在 GitHub Actions 中启用 Gradle 缓存 (actions/cache@v3)
3. 调整 `gradle.properties` 中的并行度设置

## 配置验证

### 本地验证

```bash
# 清理 Gradle 缓存
./gradlew clean

# 测试全量构建（会使用新的镜像源配置）
./gradlew build -S
```

### CI/CD 验证

在 GitHub Actions 工作流运行时查看日志：

- 搜索 "Downloaded" 来确认资源来源
- 搜索 "maven.aliyun.com" 确认使用了阿里云镜像
- 如果出现备用源 URL，说明已触发备用机制

## 未来改进

- [ ] 支持私有镜像源配置
- [ ] 添加镜像源健康检查监控
- [ ] 实现自适应源选择（根据响应时间）
- [ ] 为不同的依赖类型配置不同的源优先级
