// Parent module for examples
description = "Tars Examples - Example projects demonstrating Tars usage"

// This is just a parent module, no source code
tasks.withType<Jar> {
    enabled = false
}

tasks.withType<Javadoc> {
    enabled = false
}

subprojects {
    // Examples should not be published
    tasks.withType<PublishToMavenRepository> {
        enabled = false
    }
    tasks.withType<PublishToMavenLocal> {
        enabled = false
    }

    // Native examples need compilation enabled
    val nativeProjects = listOf("tars-native-server", "tars-native-client")
    if (project.name !in nativeProjects) {
        // Disable compilation for old examples (they depend on generated code from .tars files)
        tasks.withType<JavaCompile> {
            enabled = false
        }
        tasks.withType<Jar> {
            enabled = false
        }
        tasks.withType<Javadoc> {
            enabled = false
        }
    }
}
