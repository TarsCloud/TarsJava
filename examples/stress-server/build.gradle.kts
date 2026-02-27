plugins {
    java
    war
}

description = "Tars Stress Server Example"

dependencies {
    implementation(project(":tars-core"))

    testImplementation(libs.junit)
}
