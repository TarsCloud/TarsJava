plugins {
    `java-library`
}

description = "Tars HTTP Extension - HTTP extension for Spring Boot"

dependencies {
    api(project(":spring:tars-spring-boot-starter"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.commons.lang)

    testImplementation(libs.junit)
}
