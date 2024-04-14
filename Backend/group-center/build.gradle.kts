import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
}

group = "com.khm.group"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")

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
    implementation("com.oceanbase:oceanbase-client:2.4.9")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Crypto
    // https://mvnrepository.com/artifact/com.auth0/java-jwt
    implementation("com.auth0:java-jwt:4.4.0")

    // https://mvnrepository.com/artifact/com.tencent.kona/kona-crypto
    // https://github.com/Tencent/TencentKonaSMSuite
//    implementation("com.tencent.kona:kona-crypto")
//    implementation("com.tencent.kona:kona-provider")

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
