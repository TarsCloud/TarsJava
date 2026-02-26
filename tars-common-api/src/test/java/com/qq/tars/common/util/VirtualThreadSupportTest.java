package com.qq.tars.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class VirtualThreadSupportTest {

    @Test
    public void testExecuteServerTaskDisabledByDefault() {
        String originGlobal = System.getProperty(VirtualThreadSupport.VTHREAD_ENABLED_KEY);
        String originServer = System.getProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY);
        try {
            System.clearProperty(VirtualThreadSupport.VTHREAD_ENABLED_KEY);
            System.clearProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY);
            VirtualThreadSupport.shutdownForTest();
            Assert.assertFalse(VirtualThreadSupport.executeServerTask(() -> {
            }));
        } finally {
            restoreProperty(VirtualThreadSupport.VTHREAD_ENABLED_KEY, originGlobal);
            restoreProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY, originServer);
            VirtualThreadSupport.shutdownForTest();
        }
    }

    @Test
    public void testExecuteServerTaskWithVirtualThreadWhenEnabled() throws Exception {
        String originGlobal = System.getProperty(VirtualThreadSupport.VTHREAD_ENABLED_KEY);
        String originServer = System.getProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY);
        try {
            System.setProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY, "true");
            VirtualThreadSupport.shutdownForTest();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Thread> executedThread = new AtomicReference<>();
            boolean executed = VirtualThreadSupport.executeServerTask(() -> {
                executedThread.set(Thread.currentThread());
                latch.countDown();
            });

            if (VirtualThreadSupport.isVirtualThreadSupported()) {
                Assert.assertTrue(executed);
                Assert.assertTrue(latch.await(3, TimeUnit.SECONDS));
                Assert.assertTrue(isVirtualThread(executedThread.get()));
            } else {
                Assert.assertFalse(executed);
            }
        } finally {
            restoreProperty(VirtualThreadSupport.VTHREAD_ENABLED_KEY, originGlobal);
            restoreProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY, originServer);
            VirtualThreadSupport.shutdownForTest();
        }
    }

    private static boolean isVirtualThread(Thread thread) throws Exception {
        if (thread == null) {
            return false;
        }
        Method method = Thread.class.getMethod("isVirtual");
        return (Boolean) method.invoke(thread);
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
