package com.qq.tars.aot.api;

import java.util.List;

/**
 * Describes a JDK dynamic proxy that requires registration for GraalVM native image.
 */
public class JdkProxyDescriber {

    private final List<String> interfaces;

    public JdkProxyDescriber(List<String> interfaces) {
        this.interfaces = List.copyOf(interfaces);
    }

    public List<String> getInterfaces() {
        return interfaces;
    }
}
