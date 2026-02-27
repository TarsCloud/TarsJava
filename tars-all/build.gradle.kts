plugins {
    `java-library`
}

description = "Tars All - Aggregation module including all core Tars dependencies"

dependencies {
    api(project(":tars-common-api"))
    api(project(":tars-core"))
    api(project(":tars-context"))
    api(project(":tars-logger:tars-logger-logback"))

    testImplementation(libs.junit)
}
