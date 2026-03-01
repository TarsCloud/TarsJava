plugins {
    `java-library`
}

description = "Tars Plugins - Plugin System for Tars"

dependencies {
    api(project(":tars-core"))

    compileOnly(libs.logback.classic)

    testImplementation(libs.junit)
    testImplementation(libs.hamcrest.all)
}
