package com.qq.tars.client.rpc;

/**
 * Abstract factory for transporter.
 * Used to load different transporter implementation(such as netty, reactor,
 * restTemplate, etc) through
 * SPI:{@code com.qq.tars.client.rpc.TransporterFactory}
 *
 * @author kongyuanyuan
 */
public class TransporterAbstractFactory {

    private static final TransporterAbstractFactory INSTANCE = new TransporterAbstractFactory();
    private final TransporterFactory defaultTransporterFactory;

    private TransporterAbstractFactory() {
        defaultTransporterFactory = new com.qq.tars.rpc.netty.NettyTransporterFactory();
    }

    public static TransporterAbstractFactory getInstance() {
        return INSTANCE;
    }

    public TransporterFactory getTransporterFactory() {
        return defaultTransporterFactory;
    }
}
