package com.qq.tars.client.rpc;

import com.google.common.collect.ImmutableList;
import com.qq.tars.support.log.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Abstract factory for transporter.
 * Used to load different transporter implementation(such as netty, reactor, restTemplate, etc) through SPI:{@code com.qq.tars.client.rpc.TransporterFactory}
 *
 * @author kongyuanyuan
 */
public class TransporterAbstractFactory {
    private static final Logger log = LoggerFactory.getTransporterLogger();

    private static final TransporterAbstractFactory INSTANCE = new TransporterAbstractFactory();
    private final TransporterFactory defaultTransporterFactory;

    private TransporterAbstractFactory() {
        ServiceLoader<TransporterFactory> transporterFactories = ServiceLoader.load(TransporterFactory.class, this.getClass().getClassLoader());
        List<TransporterFactory> transporterFactoryList = ImmutableList.copyOf(transporterFactories);
        if (transporterFactoryList.isEmpty()) {
            throw new IllegalStateException("No TransporterFactory implementation found on the classpath through SPI. Try to add tars-netty in pom.xml.");
        }
        defaultTransporterFactory = transporterFactoryList.get(0);
        if (transporterFactoryList.size() > 1) {
            log.warn("More than one transporter factory found. {} will be used.", defaultTransporterFactory.getClass().getCanonicalName());
        }
    }

    public static TransporterAbstractFactory getInstance() {
        return INSTANCE;
    }

    public TransporterFactory getTransporterFactory() {
        return defaultTransporterFactory;
    }
}
