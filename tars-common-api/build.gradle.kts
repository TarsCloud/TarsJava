plugins {
    `java-library`
}

description = "Tars Common API"

dependencies {
    api(libs.guava)
    api(libs.netty.all)
    api(libs.gson)
    api(libs.jsr305)

    implementation(libs.assertj.core)
    testImplementation(libs.junit)
}
