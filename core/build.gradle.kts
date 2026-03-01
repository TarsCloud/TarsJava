plugins {
    `java-library`
}

description = "Tars Core - Core RPC Framework"

dependencies {
    api(project(":tars-common-api"))
    api(project(":tars-netty"))

    implementation(libs.servlet.api)
    implementation(libs.httpcore)
    implementation(libs.gson)

    // Tracing dependencies
    implementation(libs.zipkin.sender.kafka)
    implementation(libs.zipkin.sender.urlconnection)
    implementation(libs.brave.opentracing)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.opentracing.shim)

    testImplementation(libs.junit)
}
