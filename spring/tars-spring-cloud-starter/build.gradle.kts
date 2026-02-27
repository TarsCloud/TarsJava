plugins {
    `java-library`
}

description = "Tars Spring Cloud Starter - Spring Cloud integration for Tars"

dependencies {
    api(project(":spring:tars-spring"))
    api(project(":tars-core"))

    implementation(libs.spring.boot.starter.v2)
    implementation(libs.eureka.client)
    implementation(libs.spring.cloud.netflix.eureka.client)
    implementation(libs.spring.cloud.commons)

    testImplementation(libs.junit)
}
