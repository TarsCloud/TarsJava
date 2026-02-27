plugins {
    `java-library`
}

description = "Tars Logger Log4j2 - Log4j2 implementation for Tars logging"

dependencies {
    api(project(":tars-common-api"))

    implementation(libs.log4j.core)

    testImplementation(libs.junit)
}
