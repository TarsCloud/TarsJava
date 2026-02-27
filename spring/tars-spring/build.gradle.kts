plugins {
    `java-library`
}

description = "Tars Spring - Spring integration for Tars"

dependencies {
    api(project(":tars-core"))
    api(project(":tars-netty"))

    implementation(libs.spring.boot.starter)
    compileOnly(libs.spring.boot.configuration.processor)

    testImplementation(libs.junit)
}
