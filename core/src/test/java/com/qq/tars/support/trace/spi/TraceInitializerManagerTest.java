package com.qq.tars.support.trace.spi;

import com.qq.tars.server.config.ServerConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceInitializerManagerTest {

    @Test
    public void shouldSelectTheFirstSupportedInitializer() {
        MockTraceInitializer unsupported = new MockTraceInitializer("unsupported", false);
        MockTraceInitializer supported = new MockTraceInitializer("supported", true);
        MockTraceInitializer fallback = new MockTraceInitializer("fallback", true);

        TraceInitializerManager manager = new TraceInitializerManager(Arrays.asList(unsupported, supported, fallback));
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setSampleRate(1);
        serverConfig.setSampleType("skywalking");

        boolean initialized = manager.init(serverConfig);

        Assert.assertTrue(initialized);
        Assert.assertEquals(0, unsupported.initCounter.get());
        Assert.assertEquals(1, supported.initCounter.get());
        Assert.assertEquals(0, fallback.initCounter.get());
    }

    @Test
    public void shouldSkipInitializationWhenSampleRateIsDisabled() {
        MockTraceInitializer supported = new MockTraceInitializer("supported", true);

        TraceInitializerManager manager = new TraceInitializerManager(Arrays.asList(supported));
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setSampleRate(0);
        serverConfig.setSampleType("skywalking");

        boolean initialized = manager.init(serverConfig);

        Assert.assertFalse(initialized);
        Assert.assertEquals(0, supported.initCounter.get());
    }

    private static class MockTraceInitializer implements TraceInitializer {

        private final String name;
        private final boolean supported;
        private final AtomicInteger initCounter = new AtomicInteger(0);

        private MockTraceInitializer(String name, boolean supported) {
            this.name = name;
            this.supported = supported;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean supports(ServerConfig serverConfig) {
            return supported;
        }

        @Override
        public void init(ServerConfig serverConfig) {
            initCounter.incrementAndGet();
        }
    }
}
