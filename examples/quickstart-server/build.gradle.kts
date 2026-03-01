plugins {
    java
    war
}

description = "Tars Quickstart Server Example"

dependencies {
    implementation(project(":tars-core"))
    implementation(project(":tars-netty"))

    testImplementation(libs.junit)
}
