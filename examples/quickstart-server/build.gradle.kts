plugins {
    java
    war
}

description = "Tars Quickstart Server Example"

dependencies {
    implementation(project(":tars-core"))
    implementation(project(":tars-netty"))
    implementation(libs.sysout.over.slf4j)

    testImplementation(libs.junit)
}
