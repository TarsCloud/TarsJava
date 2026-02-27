/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package com.qq.tars.example.native_client;

import com.qq.tars.client.Communicator;
import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.CommunicatorFactory;

/**
 * GraalVM Native Image 客户端示例
 *
 * 编译命令:
 * ./gradlew :examples:tars-native-client:nativeCompile
 *
 * 运行:
 * ./examples/tars-native-client/build/native/nativeCompile/tars-native-client
 */
public class NativeClientMain {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 18601;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Tars Native Client Starting...");
        System.out.println("Server: " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("========================================");

        try {
            // 创建通信器配置
            CommunicatorConfig config = new CommunicatorConfig();

            // 创建通信器
            Communicator communicator = CommunicatorFactory.getInstance().getCommunicator(config);

            // 构建服务地址
            String objectName = "TestApp.HelloServer.HelloObj@tcp -h " + SERVER_HOST + " -p " + SERVER_PORT;

            // 创建代理
            HelloPrx helloPrx = communicator.stringToProxy(HelloPrx.class, objectName);

            System.out.println("========================================");
            System.out.println("Calling remote methods...");
            System.out.println("========================================");

            // 调用hello方法
            String helloResult = helloPrx.hello("GraalVM Native");
            System.out.println("hello('GraalVM Native') = " + helloResult);

            // 调用add方法
            int addResult = helloPrx.add(100, 200);
            System.out.println("add(100, 200) = " + addResult);

            System.out.println("========================================");
            System.out.println("All tests passed!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
