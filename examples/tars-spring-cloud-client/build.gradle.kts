plugins {
    java
}

description = "Tars Spring Cloud Client Example"

dependencies {
    implementation(project(":spring:tars-spring-cloud-starter"))
    implementation(project(":tars-core"))

    testImplementation(libs.junit)
}
