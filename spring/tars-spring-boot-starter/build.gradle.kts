plugins {
    `java-library`
}

description = "Tars Spring Boot Starter - Spring Boot starter for Tars"

dependencies {
    api(project(":spring:tars-spring"))

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)
    compileOnly(libs.spring.boot.configuration.processor)

    testImplementation(libs.junit)
}
