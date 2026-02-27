plugins {
    java
    application
    id("org.graalvm.buildtools.native")
}

description = "Tars Native Server Example - GraalVM Native Image"

application {
    mainClass.set("com.qq.tars.example.native_server.NativeServerMain")
}

dependencies {
    implementation(project(":tars-core"))
    implementation(project(":tars-netty"))
    implementation(libs.logback.classic)

    testImplementation(libs.junit)
}

graalvmNative {
    binaries {
        named("main") {
            mainClass.set("com.qq.tars.example.native_server.NativeServerMain")
            buildArgs.addAll(
                "--no-fallback",
                "-H:+ReportExceptionStackTraces",
                "-H:+AddAllCharsets"
            )
        }
    }
}
