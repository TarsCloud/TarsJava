package com.qq.tars.aot;

/**
 * Detects whether the current runtime is a GraalVM native image.
 */
public final class NativeDetector {

    private static final boolean IN_NATIVE_IMAGE =
            System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    private NativeDetector() {
    }

    /**
     * Returns {@code true} if running inside a GraalVM native image.
     */
    public static boolean inNativeImage() {
        return IN_NATIVE_IMAGE;
    }
}
