// Parent module for Spring integrations
description = "Tars Spring Parent - Parent module for Spring integrations"

// This is just a parent module, no source code
tasks.withType<Jar> {
    enabled = false
}

tasks.withType<Javadoc> {
    enabled = false
}
