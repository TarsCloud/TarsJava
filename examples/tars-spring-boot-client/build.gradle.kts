plugins {
    java
}

description = "Tars Spring Boot Client Example"

dependencies {
    implementation(project(":spring:tars-spring-boot-starter"))

    testImplementation(libs.junit)
}
