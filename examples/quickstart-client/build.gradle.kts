plugins {
    java
}

description = "Tars Quickstart Client Example"

dependencies {
    implementation(project(":tars-core"))
    implementation(project(":tars-netty"))

    testImplementation(libs.junit)
}
