dependencies {
    implementation(project(":bank-domain"))
    implementation(project(":bank-monitoring"))
    implementation(project(":bank-core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.springframework.retry:spring-retry")
}