rootProject.name = "group-center"

// https://www.cnblogs.com/bluestorm/p/14761482.html
// https://blog.csdn.net/weixin_47879762/article/details/132979766

// https://developer.aliyun.com/mirror/
// https://developer.aliyun.com/mirror/maven

pluginManagement {
    repositories {
        // 阿里云镜像（主源）
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/spring") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // maven { setUrl("https://jitpack.io") }
        // maven { url = uri("https://www.jitpack.io") }
        // google()

        // MyBatis-Plus Snapshot
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }

        // 官方源（备用，在阿里云无法访问时使用）
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云镜像（主源）
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // maven { url = uri("https://jitpack.io") }
        // maven { url = uri("https://www.jitpack.io") }
        // google()

        maven { url = uri("https://maven.aliyun.com/repository/spring") }
        
        // MyBatis-Plus Snapshot
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        
        // 官方源（备用，在阿里云无法访问时使用）
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}
