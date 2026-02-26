package com.qq.tars.support.trace.spi;

import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.support.om.OmConstants;
import com.qq.tars.support.trace.TraceManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;

public class ZipkinTraceInitializerTest {

    @Test
    public void shouldSupportZipkinSampleTypes() {
        ZipkinTraceInitializer initializer = new ZipkinTraceInitializer();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setSampleType("http");
        Assert.assertTrue(initializer.supports(serverConfig));

        serverConfig.setSampleType("kafka");
        Assert.assertTrue(initializer.supports(serverConfig));

        serverConfig.setSampleType("skywalking");
        Assert.assertFalse(initializer.supports(serverConfig));
    }

    @Test
    public void shouldRegisterTracerForBusinessServant() throws Exception {
        ZipkinTraceInitializer initializer = new ZipkinTraceInitializer();
        String servantName = "TestApp.TestServer.ZipkinObj" + System.nanoTime();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setSampleType("http");
        serverConfig.setSampleRate(1);
        serverConfig.setSampleEncoding("json");
        serverConfig.setSampleAddress("http://127.0.0.1:9411");

        LinkedHashMap<String, com.qq.tars.server.config.ServantAdapterConfig> servantMap = new LinkedHashMap<>();
        servantMap.put(servantName, null);
        servantMap.put(OmConstants.AdminServant, null);
        serverConfig.setServantAdapterConfMap(servantMap);

        initializer.init(serverConfig);

        Assert.assertNotNull(TraceManager.getInstance().getCurrentTracer(servantName));
    }
}
