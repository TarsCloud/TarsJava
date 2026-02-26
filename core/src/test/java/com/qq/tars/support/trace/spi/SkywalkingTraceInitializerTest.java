package com.qq.tars.support.trace.spi;

import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.support.om.OmConstants;
import com.qq.tars.support.trace.TraceManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;

public class SkywalkingTraceInitializerTest {

    @Test
    public void shouldSupportSkywalkingSampleType() {
        SkywalkingTraceInitializer initializer = new SkywalkingTraceInitializer();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setSampleType("skywalking");
        Assert.assertTrue(initializer.supports(serverConfig));

        serverConfig.setSampleType("sw");
        Assert.assertTrue(initializer.supports(serverConfig));

        serverConfig.setSampleType("http");
        Assert.assertFalse(initializer.supports(serverConfig));
    }

    @Test
    public void shouldRegisterTracerForBusinessServant() {
        SkywalkingTraceInitializer initializer = new SkywalkingTraceInitializer();
        String servantName = "TestApp.TestServer.SkywalkingObj" + System.nanoTime();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setSampleType("skywalking");
        serverConfig.setSampleRate(1);
        serverConfig.setSampleEncoding("grpc");
        serverConfig.setSampleAddress("127.0.0.1:11800");

        LinkedHashMap<String, com.qq.tars.server.config.ServantAdapterConfig> servantMap = new LinkedHashMap<>();
        servantMap.put(servantName, null);
        servantMap.put(OmConstants.AdminServant, null);
        serverConfig.setServantAdapterConfMap(servantMap);

        initializer.init(serverConfig);

        Assert.assertNotNull(TraceManager.getInstance().getCurrentTracer(servantName));
    }
}
