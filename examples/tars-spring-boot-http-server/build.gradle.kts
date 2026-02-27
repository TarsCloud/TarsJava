plugins {
    java
}

description = "Tars Spring Boot HTTP Server Example"

dependencies {
    implementation(project(":spring:tars-http-extension"))

    testImplementation(libs.junit)
}
