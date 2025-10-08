# Controller

## Data Class Serializable

请注意下面的问题，这是一个示例，问题分析：

```txt
MachineHeartbeat 类使用了 kotlinx.serialization.Serializable 注解和 val 不可变字段
FastJson 配置中启用了 JSONReader.Feature.FieldBased 特性
这两者不兼容，导致 FastJson 无法正确反序列化 JSON 数据
修复方案：
我修改了 MachineHeartbeat.kt 类：

移除了 kotlinx.serialization.Serializable 注解
将 val 不可变字段改为 var 可变字段
为字段添加了默认值
```
