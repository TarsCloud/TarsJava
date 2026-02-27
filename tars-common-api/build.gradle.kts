plugins {
    `java-library`
}

description = "Tars Common API"

dependencies {
    api(libs.guava)
    api(libs.slf4j.api)
    api(libs.netty.all)
    api(libs.gson)
    api(libs.jsr305)

    implementation(libs.assertj.core)
    testImplementation(libs.junit)
}
