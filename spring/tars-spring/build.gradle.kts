plugins {
    `java-library`
}

description = "Tars Spring - Spring integration for Tars"

dependencies {
    api(project(":tars-core"))
    api(project(":tars-netty"))

    implementation(libs.spring.boot.starter.v1)
    compileOnly(libs.spring.boot.configuration.processor.v1)

    testImplementation(libs.junit)
}
