plugins {
    `java-library`
}

description = "Tars All - Aggregation module including all core Tars dependencies"

dependencies {
    api(project(":tars-common-api"))
    api(project(":tars-core"))

    testImplementation(libs.junit)
}
