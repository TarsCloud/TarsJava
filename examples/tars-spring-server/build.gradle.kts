plugins {
    java
    war
}

description = "Tars Spring Server Example"

dependencies {
    implementation(project(":spring:tars-spring"))
    implementation(project(":tars-core"))

    testImplementation(libs.junit)
}
