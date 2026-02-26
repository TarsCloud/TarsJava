plugins {
    `java-library`
}

description = "Tars Netty - Netty-based Transport Layer"

val nettyVersion = libs.netty.all.get().versionConstraint.requiredVersion
val osName = System.getProperty("os.name").lowercase()
val archName = System.getProperty("os.arch").lowercase()
val nettyArch = when (archName) {
    "x86_64", "amd64" -> "x86_64"
    "aarch64", "arm64" -> "aarch_64"
    else -> null
}

dependencies {
    api(project(":tars-common-api"))

    implementation(libs.netty.all)
    implementation(libs.netty.tcnative)

    if (nettyArch != null && osName.contains("linux")) {
        runtimeOnly("io.netty:netty-transport-native-epoll:$nettyVersion:linux-$nettyArch")
    } else if (nettyArch != null && (osName.contains("mac") || osName.contains("darwin"))) {
        runtimeOnly("io.netty:netty-transport-native-kqueue:$nettyVersion:osx-$nettyArch")
        runtimeOnly("io.netty:netty-resolver-dns-native-macos:$nettyVersion:osx-$nettyArch")
    }

    testImplementation(libs.junit)
}
