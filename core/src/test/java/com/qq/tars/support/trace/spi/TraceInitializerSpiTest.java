package com.qq.tars.support.trace.spi;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public class TraceInitializerSpiTest {

    @Test
    public void shouldLoadBuiltInTraceInitializersFromSpi() {
        ServiceLoader<TraceInitializer> loaders = ServiceLoader.load(TraceInitializer.class);
        Set<String> providerNames = new HashSet<>();
        for (TraceInitializer initializer : loaders) {
            providerNames.add(initializer.name());
        }

        Assert.assertTrue(providerNames.contains("zipkin"));
        Assert.assertTrue(providerNames.contains("skywalking"));
    }
}
