import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val kotlinVersion = "2.2.21"
    val springBootVersion = "3.5.7"

    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.1.7"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion

    id("com.github.ben-manes.versions") version "0.52.0"
}

val kotlinVersion = "2.2.21"
// val kotlinVersionPrevious = "2.2.20"

val springBootVersion = "3.5.7"
val myBatisVersion = "3.0.5"

val fastjsonVersion = "2.0.59"
//val fastjsonVersion = "2.0.60"

group = "com.khm.group"
// version = "1.5.5-SNAPSHOT"
version = "1.5.5"

java {
    sourceCompatibility = JavaVersion.VERSION_24
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any {
        version.uppercase().contains(it)
    }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

allprojects {
    apply<com.github.benmanes.gradle.versions.VersionsPlugin>()

    tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
        // configure the task, for example wrt. resolution strategies

        checkForGradleUpdate = true
        outputFormatter = "txt"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"

        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }
}

dependencies {
    ////////////////////////////////////////////////////////////////////////////////////////////
    // Kotlin
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-gradle-plugin
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-reflect
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    // https://central.sonatype.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.0")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web:${springBootVersion}")
    implementation("com.alibaba.fastjson2:fastjson2-extension-spring6:${fastjsonVersion}")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Database
    // https://mvnrepository.com/artifact/com.alibaba/druid-spring-boot-starter
    implementation("com.alibaba:druid-spring-boot-starter:1.2.27")

    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:${myBatisVersion}")
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.14")

    // https://mvnrepository.com/artifact/com.gitee.sunchenbin.mybatis.actable/mybatis-enhance-actable
//    implementation("com.gitee.sunchenbin.mybatis.actable:mybatis-enhance-actable:1.5.0.RELEASE")

    // Database Driver
    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    runtimeOnly("com.mysql:mysql-connector-j:9.5.0")
//    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    // https://mvnrepository.com/artifact/com.oceanbase/oceanbase-client
    // runtimeOnly("com.oceanbase:oceanbase-client:2.4.9")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Task
    // // https://mvnrepository.com/artifact/org.jobrunr/jobrunr
    // implementation("org.jobrunr:jobrunr:8.0.2")
    // // https://mvnrepository.com/artifact/org.jobrunr/jobrunr-kotlin-1.7-support
    // implementation("org.jobrunr:jobrunr-kotlin-1.7-support:7.2.0")
    // // https://mvnrepository.com/artifact/org.jobrunr/jobrunr-spring-boot-starter
    // implementation("org.jobrunr:jobrunr-spring-boot-starter:5.3.3")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Security and Crypto

    // implementation("org.springframework.boot:spring-boot-starter-security:${springBootVersion}")

    // https://mvnrepository.com/artifact/com.auth0/java-jwt
//    implementation("com.auth0:java-jwt:4.4.0")

    // https://mvnrepository.com/artifact/com.tencent.kona/kona-crypto
    // https://github.com/Tencent/TencentKonaSMSuite
    implementation("com.tencent.kona:kona-crypto:1.0.18")

    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation("commons-codec:commons-codec:1.17.1")
    ///////////////////////////////////////////////////////////////////////////////////////////
    // Message WebHook
    implementation("com.squareup.okhttp3:okhttp:5.2.1")

    // https://mvnrepository.com/artifact/com.larksuite.oapi/oapi-sdk
    implementation("com.larksuite.oapi:oapi-sdk:2.4.24")

    // Data
    // https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2
//    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:${fastjsonVersion}")

    // Config File
    implementation("com.akuleshov7:ktoml-core:0.7.0")
    implementation("com.akuleshov7:ktoml-file:0.7.0")

    implementation("com.charleskorn.kaml:kaml:0.67.0")

    // File Encoding
    implementation("com.github.albfernandez:juniversalchardet:2.5.0")

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Dev Tools
    // Lombok for Java
    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    compileOnly("org.projectlombok:lombok:1.18.42")

    // Docker
    // runtimeOnly("org.springframework.boot:spring-boot-docker-compose")

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
    implementation("org.springframework.boot:spring-boot-starter-actuator:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-actuator-autoconfigure:${springBootVersion}")

    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-devtools
    implementation("org.springframework.boot:spring-boot-devtools:${springBootVersion}")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-gradle-plugin
    implementation("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:${springBootVersion}")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:${myBatisVersion}")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    // testImplementation("io.mockk:mockk:1.13.12")
}

// Output Program Version
//tasks.create<WriteProperties>("writeVersionProperties") {
//    group = "export"
//    property("version", project.version.toString())
//    destinationFile = file("src/main/resources/settings/version.properties")
//}
tasks.register<WriteProperties>("writeVersionProperties") {
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
        jvmTarget.set(JvmTarget.JVM_24)
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
