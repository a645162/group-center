# 输出规则

请使用log日志系统来进行调试输出

```kotlin
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger

logger.info("Receive User($userNameEng) FileName($fileName)")
```

Kotlin中导入完包，就可以直接使用logger.info或者logger.debug，请你区分好不同的日志级别！
