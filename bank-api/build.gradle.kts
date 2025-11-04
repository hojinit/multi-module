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