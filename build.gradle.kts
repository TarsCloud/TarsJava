import java.time.Instant

plugins {
    java
    `maven-publish`
    signing
}

val tarsVersion = "2.0.0"

allprojects {
    group = "com.tencent.tars"
    version = tarsVersion

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc> {
        options {
            (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        }
    }

    tasks.withType<Jar> {
        manifest {
            attributes["Built-By"] = "gradle"
            attributes["Built-Time"] = Instant.now().toString()
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set("Tars Java - High performance RPC framework")
                    url.set("https://github.com/TarsCloud/TarsJava")

                    licenses {
                        license {
                            name.set("The BSD 3-Clause License")
                            url.set("https://opensource.org/licenses/BSD-3-Clause")
                        }
                    }

                    developers {
                        developer {
                            name.set("tencent")
                            email.set("tars@tencent.com")
                            organization.set("Tencent")
                            organizationUrl.set("https://github.com/TarsCloud")
                        }
                    }

                    scm {
                        connection.set("scm:git:https://github.com/TarsCloud/TarsJava.git")
                        developerConnection.set("scm:git:https://github.com/TarsCloud/TarsJava.git")
                        url.set("https://github.com/TarsCloud/TarsJava")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "sonatype"
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

                credentials {
                    username = findProperty("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME")
                    password = findProperty("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD")
                }
            }
        }
    }

    signing {
        setRequired {
            !version.toString().endsWith("SNAPSHOT") && gradle.taskGraph.hasTask("publish")
        }
        sign(publishing.publications["mavenJava"])
    }

    tasks.withType<Test> {
        useJUnit()
    }
}

// Skip publishing for parent/aggregation modules
listOf("tars-parent", "tars-spring-parent", "tars-tools", "tars-examples", "tars-logger").forEach { moduleName ->
    project.subprojects.find { it.name == moduleName }?.afterEvaluate {
        tasks.withType<PublishToMavenRepository> {
            enabled = false
        }
        tasks.withType<PublishToMavenLocal> {
            enabled = false
        }
    }
}
