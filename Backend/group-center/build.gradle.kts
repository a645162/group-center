import org.jetbrains.kotlin.gradle.dsl.JvmTarget

//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

group = "com.khm.group"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

// https://developer.aliyun.com/mirror/
// https://developer.aliyun.com/mirror/maven
allprojects {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/spring") }

        mavenLocal()
        mavenCentral()
    }
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/spring") }

    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

    mavenLocal()
    mavenCentral()

    gradlePluginPortal()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.alibaba.fastjson2:fastjson2-extension-spring6:2.0.51")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Database
    // https://mvnrepository.com/artifact/com.alibaba/druid-spring-boot-starter
    implementation("com.alibaba:druid-spring-boot-starter:1.2.22")

    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.6")

    // Database Driver
    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    runtimeOnly("com.mysql:mysql-connector-j:8.3.0")
//    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    // https://mvnrepository.com/artifact/com.oceanbase/oceanbase-client
    runtimeOnly("com.oceanbase:oceanbase-client:2.4.9")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Crypto
    // https://mvnrepository.com/artifact/com.auth0/java-jwt
//    implementation("com.auth0:java-jwt:4.4.0")

    // https://mvnrepository.com/artifact/com.tencent.kona/kona-crypto
    // https://github.com/Tencent/TencentKonaSMSuite
    implementation("com.tencent.kona:kona-crypto:1.0.11")

    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation("commons-codec:commons-codec:1.17.0")
    ///////////////////////////////////////////////////////////////////////////////////////////
    // Message WebHook
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // https://mvnrepository.com/artifact/com.larksuite.oapi/oapi-sdk
    implementation("com.larksuite.oapi:oapi-sdk:2.2.8")

    // Data
    // https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2
//    implementation("com.alibaba.fastjson2:fastjson2:2.0.51")
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.50")

    // Config File
    implementation("com.akuleshov7:ktoml-core:0.5.1")
    implementation("com.akuleshov7:ktoml-file:0.5.1")

    implementation("com.charleskorn.kaml:kaml:0.59.0")

    // File Encoding
    implementation("com.github.albfernandez:juniversalchardet:2.5.0")

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Dev Tools
    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    compileOnly("org.projectlombok:lombok:1.18.32")
    ///////////////////////////////////////////////////////////////////////////////////////////
    // Document
    // https://mvnrepository.com/artifact/io.swagger.core.v3/swagger-core
//    implementation("io.swagger.core.v3:swagger-core:2.2.21")
    // https://mvnrepository.com/artifact/io.swagger.core.v3/swagger-annotations
//    implementation("io.swagger.core.v3:swagger-annotations:2.2.21")
    // https://mvnrepository.com/artifact/io.swagger.core.v3/swagger-models
//    implementation("io.swagger.core.v3:swagger-models:2.2.21")

    // https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-ui
//    implementation("org.springdoc:springdoc-openapi-ui:1.8.0")

    // https://doc.xiaominfo.com/
    // https://mvnrepository.com/artifact/com.github.xiaoymin/knife4j-openapi3-jakarta-spring-boot-starter
    implementation("com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter:4.5.0")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Operations and Maintenance
    // Spring Boot Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-devtools
    implementation("org.springframework.boot:spring-boot-devtools:3.3.0")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-gradle-plugin
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.3.0")
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.3")
}

// Output Program Version
tasks.create<WriteProperties>("writeVersionProperties") {
    group = "export"
    property("version", project.version.toString())
    destinationFile = file("src/main/resources/settings/version.properties")
}

tasks.withType<ProcessResources> {
    dependsOn("writeVersionProperties")
}

// https://kotlinlang.org/docs/gradle-compiler-options.html#target-the-jvm
tasks.named(
    "compileKotlin",
    org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java
) {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions {
//        freeCompilerArgs += "-Xjsr305=strict"
//        jvmTarget = "21"
//    }
//}

tasks.withType<Test> {
    useJUnitPlatform()
}
