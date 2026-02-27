plugins {
    `java-library`
}

description = "Tars Spring Boot Starter - Spring Boot 2.x starter for Tars"

dependencies {
    api(project(":spring:tars-spring"))

    implementation(libs.spring.boot.starter.v2)
    implementation(libs.spring.boot.starter.web)
    compileOnly(libs.spring.boot.configuration.processor.v2)

    testImplementation(libs.junit)
}
