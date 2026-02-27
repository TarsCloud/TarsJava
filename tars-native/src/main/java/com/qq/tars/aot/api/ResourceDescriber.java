package com.qq.tars.aot.api;

/**
 * Describes a resource pattern that must be included in the GraalVM native image.
 */
public class ResourceDescriber {

    private final String pattern;

    public ResourceDescriber(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
