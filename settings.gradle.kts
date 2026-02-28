pluginManagement {
    plugins {
        id("org.graalvm.buildtools.native") version "0.10.6"
    }
}

rootProject.name = "tars-parent"

// Enable version catalog
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Core modules
include("tars-common-api")
include("tars-netty")
include("core")
project(":core").name = "tars-core"
include("tars-plugins")

// Logger modules
include("tars-logger")
include("tars-logger:tars-logger-logback")
include("tars-logger:tars-logger-log4j")
include("tars-logger:tars-logger-log4j2")

// Spring modules
include("spring")
project(":spring").name = "tars-spring-parent"
include("spring:tars-spring")
include("spring:tars-spring-boot-starter")
include("spring:tars-spring-cloud-starter")
include("spring:tars-http-extension")

// Native image support
include("tars-native")

// Aggregation module
include("tars-all")

// Examples
include("examples")
project(":examples").name = "tars-examples"
include("examples:quickstart-client")
include("examples:quickstart-server")
include("examples:stress-server")
include("examples:tars-spring-boot-client")
include("examples:tars-spring-boot-http-server")
include("examples:tars-spring-boot-server")
include("examples:tars-spring-cloud-client")
include("examples:tars-spring-cloud-server")
include("examples:tars-spring-server")
include("examples:tars-native-server")
include("examples:tars-native-client")

