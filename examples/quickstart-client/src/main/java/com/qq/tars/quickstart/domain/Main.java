/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.qq.tars.quickstart.domain;

import com.qq.tars.client.Communicator;
import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.CommunicatorFactory;
import com.qq.tars.quickstart.client.testapp.HelloPrx;
import com.qq.tars.quickstart.client.testapp.HelloPrxCallback;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws InterruptedException {
        // Start configuration locally
        CommunicatorConfig cfg = new CommunicatorConfig();
        // Start communicator locally
        Communicator communicator = CommunicatorFactory.getInstance().getCommunicator(cfg);
        //warn If the deployment is started on the tars, you can only use the following constructor to get the communicator
        //Communicator communicator = CommunicatorFactory.getInstance().getCommunicator();
        HelloPrx proxy = communicator.stringToProxy(HelloPrx.class, "TestApp.HelloServer.HelloObj@tcp -h 127.0.0.1 -p 18601 -t 60000");
        //Synchronous call
        String ret = proxy.hello(1000, "Hello World");
        System.out.println(ret);

        //One-way call
        proxy.async_hello(null, 1000, "Hello World");

        //Asynchronous call
        proxy.async_hello(new HelloPrxCallback() {

            @Override
            public void callback_expired() {
            }

            @Override
            public void callback_exception(Throwable ex) {
            }

            @Override
            public void callback_hello(String ret) {
                Main.logger.log(Level.INFO, "invoke use async {0}", ret);

            }
        }, 1000, "Hello World");

        proxy.promise_hello(1000, "hello world").thenCompose(x -> {
            logger.log(Level.INFO, "invoke use promise {0}", x);
            return CompletableFuture.completedFuture(0);
        });

        Thread.sleep(1000l);
    }
}
