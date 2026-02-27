plugins {
    `java-library`
}

description = "Tars Logger Logback - Logback implementation for Tars logging"

dependencies {
    api(project(":tars-common-api"))

    implementation(libs.logback.classic)

    testImplementation(libs.junit)
}
