package com.qq.tars.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public final class VirtualThreadSupport {
    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadSupport.class);

    public static final String VTHREAD_ENABLED_KEY = "tars.virtual.threads.enabled";
    public static final String SERVER_VTHREAD_ENABLED_KEY = "tars.virtual.threads.server.enabled";

    private static volatile ExecutorService serverVirtualExecutor;
    private static volatile boolean unsupportedLogged = false;

    private VirtualThreadSupport() {
    }

    public static boolean isVirtualThreadSupported() {
        try {
            Method factoryMethod = Thread.class.getMethod("ofVirtual");
            return factoryMethod != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean isServerVirtualThreadEnabled() {
        String serverEnabled = System.getProperty(SERVER_VTHREAD_ENABLED_KEY);
        if (serverEnabled != null) {
            return Boolean.parseBoolean(serverEnabled);
        }
        return Boolean.parseBoolean(System.getProperty(VTHREAD_ENABLED_KEY, "false"));
    }

    public static boolean executeServerTask(Runnable task) {
        Objects.requireNonNull(task, "task");
        if (!isServerVirtualThreadEnabled()) {
            return false;
        }
        if (!isVirtualThreadSupported()) {
            logUnsupportedOnce();
            return false;
        }

        ExecutorService executor = getOrCreateServerExecutor();
        if (executor == null) {
            return false;
        }
        try {
            executor.execute(task);
            return true;
        } catch (RejectedExecutionException e) {
            logger.warn("[tars] submit virtual thread task failed, {}", e.getMessage());
            return false;
        }
    }

    static void shutdownForTest() {
        ExecutorService executor = serverVirtualExecutor;
        serverVirtualExecutor = null;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private static ExecutorService getOrCreateServerExecutor() {
        ExecutorService executor = serverVirtualExecutor;
        if (executor != null) {
            return executor;
        }
        synchronized (VirtualThreadSupport.class) {
            executor = serverVirtualExecutor;
            if (executor != null) {
                return executor;
            }
            serverVirtualExecutor = createVirtualThreadExecutor();
            return serverVirtualExecutor;
        }
    }

    private static ExecutorService createVirtualThreadExecutor() {
        try {
            Method method = java.util.concurrent.Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            return (ExecutorService) method.invoke(null);
        } catch (Throwable e) {
            logger.warn("[tars] create virtual thread executor failed, {}", e.getMessage());
            return null;
        }
    }

    private static void logUnsupportedOnce() {
        if (unsupportedLogged) {
            return;
        }
        synchronized (VirtualThreadSupport.class) {
            if (unsupportedLogged) {
                return;
            }
            unsupportedLogged = true;
            logger.warn("[tars] virtual threads are enabled but current JDK does not support them");
        }
    }
}
