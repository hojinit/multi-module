dependencies {
    implementation(project(":bank-core"))
    implementation(project(":bank-domain"))
    implementation(project(":bank-event"))
    implementation(project(":bank-monitoring"))

    // H2
    runtimeOnly("com.h2database:h2")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("org.springframework.boot:spring-boot-starter-web")

    //jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // swagger
    // OpenAPI는 RESTful API를 기술하기 위한 표준 사양(Specification) > 무엇을(what)
    // Swagger는 이 OpenAPI 사양을 기반으로 API 문서를 생성하고 시각화하며 테스트하는 데 사용되는 도구 모음 > 어떻게(how)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // circuit
    implementation("io.github.resilience4j:resilience4j-spring-boot2:2.0.2")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.0.2")
    implementation("io.github.resilience4j:resilience4j-retry:2.0.2")

    // slf4j
    implementation("ch.qos.logback:logback-classic:1.4.14")
}
// public
// http://localhost:<port>/swagger-ui/index.html