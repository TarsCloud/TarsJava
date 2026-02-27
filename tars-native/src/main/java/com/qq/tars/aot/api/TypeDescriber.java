package com.qq.tars.aot.api;

/**
 * Describes a type that requires reflection registration for GraalVM native image.
 */
public class TypeDescriber {

    private final String className;
    private final boolean allDeclaredConstructors;
    private final boolean allDeclaredMethods;
    private final boolean allDeclaredFields;

    public TypeDescriber(String className, boolean allDeclaredConstructors,
                         boolean allDeclaredMethods, boolean allDeclaredFields) {
        this.className = className;
        this.allDeclaredConstructors = allDeclaredConstructors;
        this.allDeclaredMethods = allDeclaredMethods;
        this.allDeclaredFields = allDeclaredFields;
    }

    public String getClassName() {
        return className;
    }

    public boolean isAllDeclaredConstructors() {
        return allDeclaredConstructors;
    }

    public boolean isAllDeclaredMethods() {
        return allDeclaredMethods;
    }

    public boolean isAllDeclaredFields() {
        return allDeclaredFields;
    }
}
