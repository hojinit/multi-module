dependencies {
    implementation("org.springframework:spring-context")
    // Metric 수집 시 핵심, Micrometer라는 JVM 기반 App 성능과 동작 모니터링 시 사용
    // Prometheus와 같은 모니터링 시스템과 연동하는 표준 API 제공
    implementation("io.micrometer:micrometer-core")
}