// build시 실행, build에 관한 부부만 명시
// java 17 기준
plugins {
//    apply false : 선언하되 즉시 적용되지 않으므로 공통 설정과 개별 모듈 설정 분리 하기 위해 사용 (중앙 집중식 플러그인 관리)
    id("org.springframework.boot") version "3.2.3" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false   // di 사용 시 사능
    kotlin("jvm") version "2.0.21" apply false  // kotlin을 jvm으로 compile 시 사용
    kotlin("plugin.spring") version "2.0.21" apply false
    kotlin("plugin.jpa") version "2.0.21" apply false   //쿼리는 jpa로 사용
}

allprojects {
    group = "org.ktor_lecture"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
// 내부 모듈에 적용하는 부분을 나누어서 명시
// 모듈마다 불필요한 부분들은 제외하기 위해 분기처리
subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")

    if (name == "bank-api") {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
        apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    }

    if (name == "bank-core") {
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    }

    if (name == "bank-domain") {
        apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    }

    if (name == "bank-event") {
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.3")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xjsr305=strict"
        }
    }
}
