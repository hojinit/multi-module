// gradle 초기화시 실행됨, setting의 전반적인 부분을 가지고 있음
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "multi-module-lecture"

// 직접 명시 필요
include("bank-api")
include("bank-core")
include("bank-domain")
include("bank-event")
include("bank-monitoring")

// cf. plugin-management로도 관리 가능
/*
pluginManagement {
    //아래에 plugins를 명시하게 되면 build.gradle.kts에 추가 명시하지 않아도 됨
    plugins{
        id("org.springframework.boot") version "2.23"
    }
    repositories{
        gradlePluginPortal()
        mavenCentral()
    }
}
 */