plugins {
    java
    war
}

description = "Tars Spring Cloud Server Example"

dependencies {
    implementation(project(":spring:tars-spring-cloud-starter"))
    implementation(project(":spring:tars-spring"))
    implementation(project(":tars-core"))

    testImplementation(libs.junit)
}
