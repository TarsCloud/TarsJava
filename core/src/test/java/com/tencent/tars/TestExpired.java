package com.tencent.tars;


import com.qq.tars.client.Communicator;
import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.CommunicatorFactory;
import com.tencent.tars.testapp.HelloPrx;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class TestExpired {
    @Test
    public void testExpired() throws InterruptedException {
//        CommunicatorConfig cfg = new CommunicatorConfig();
//        Communicator communicator = CommunicatorFactory.getInstance().getCommunicator(cfg);
//        HelloPrx proxy = communicator.stringToProxy(HelloPrx.class, "TestServer.HelloServer.HelloObj@tcp -h 127.0.0.1 -p 18605 -t 60000");
//        proxy.promise_hello(1000, "hello world").thenCompose(x -> {
//            try {
//                // triggle ticket expired
//                Thread.sleep(5000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("invoke use promise " + x);
//            return CompletableFuture.completedFuture(0);
//        });
//        Thread.sleep(10000l);
    }
}
