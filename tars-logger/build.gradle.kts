// Parent module for logger implementations
description = "Tars Logger - Parent module for logging implementations"

// This is just a parent module, no source code
tasks.withType<Jar> {
    enabled = false
}

tasks.withType<Javadoc> {
    enabled = false
}
