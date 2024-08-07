rootProject.name = "group-center"

// https://www.cnblogs.com/bluestorm/p/14761482.html
// https://blog.csdn.net/weixin_47879762/article/details/132979766

pluginManagement {
    repositories {
        // 改为阿里云的镜像地址
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
        // maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        // maven { setUrl("https://jitpack.io") }
        gradlePluginPortal()
        // google()
        mavenCentral()
    }
}

//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        // 改为阿里云的镜像地址
//        maven { setUrl("https://maven.aliyun.com/repository/central") }
//        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
//        maven { setUrl("https://maven.aliyun.com/repository/google") }
//        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
//        maven { setUrl("https://maven.aliyun.com/repository/public") }
//        maven { setUrl("https://jitpack.io") }
//        google()
//        mavenCentral()
//    }
//}
