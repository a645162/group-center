package com.khm.group.center.utils.program

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// https://github.com/gkdgkdgkdgkd/KotlinLogWithAnnotationDemo
// https://blog.csdn.net/qq_27525611/article/details/114910325
// https://zhuanlan.zhihu.com/p/357666365

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Slf4jKt {
    companion object {
        val <reified T> T.logger: Logger
            inline get() = LoggerFactory.getLogger(T::class.java)
    }
}
