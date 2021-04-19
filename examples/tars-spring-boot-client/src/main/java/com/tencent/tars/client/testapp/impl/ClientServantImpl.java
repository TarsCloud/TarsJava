package com.tencent.tars.client.testapp.impl;

import com.qq.tars.spring.annotation.TarsClient;
import com.qq.tars.spring.annotation.TarsServant;
import com.tencent.tars.client.testapp.ClientServant;
import com.tencent.tars.client.testapp.HelloPrx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;


@TarsServant("ClientObj")
public class ClientServantImpl implements ClientServant {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientServantImpl.class);
    @TarsClient("TestServer.HelloServer.HelloObj")
    HelloPrx helloPrx;

    @Override
    public String rpcHello(int no, String name) {
        String syncResult = helloPrx.hello(1000, name);
        LOGGER.info("sync Result {}", syncResult);
        //promise调用
        helloPrx.promise_hello(1000, name).thenCompose(x -> {
            String res = "promise_result: " + x;
            LOGGER.info("promise result :{}", res);
            return CompletableFuture.completedFuture(0);
        });
        return syncResult;
    }
}
