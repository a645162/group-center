package com.khm.group.center.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Jackson全局ObjectMapper配置
 * 
 * 说明：
 * 1. Spring Boot 4.x 不再默认将ObjectMapper注册为全局Bean。
 *    如果你需要在业务类（如Service等）通过@Autowired注入ObjectMapper，
 *    必须在配置类中手动声明。
 * 2. 本配置类会注册一个全局唯一的ObjectMapper实例，
 *    推荐如需自定义特性等，也统一在这里集中定制。
 * 3. 该Bean与Http消息转换（REST API序列化/反序列化）使用的ObjectMapper是同一个（只要没自定义覆盖），避免配置割裂。
 *
 * 参考：
 * https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.json
 * 官方说明：如有业务需要请自行注册ObjectMapper Bean。
 */
@Configuration
class JacksonConfig {

    /**
     * 注册全局唯一ObjectMapper Bean，使@Autowired或@Inject可用。
     *
     * 可在此配置自定义属性、模块等。
     */
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerKotlinModule() // 支持Kotlin数据类/空安全
        // .enable(...) // 可以自定义序列化特性，如需特殊配置请补充
    }
}
