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
package com.qq.tars.example.native_server;

import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import com.qq.tars.client.rpc.TransporterServer;
import com.qq.tars.common.support.Endpoint;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.netty.NettyTransporterServer;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.rpc.protocol.tars.support.AnalystManager;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.core.Processor;
import io.netty.channel.Channel;

import java.nio.charset.Charset;

/**
 * GraalVM Native Image 服务端示例
 *
 * 编译命令:
 * ./gradlew :examples:tars-native-server:nativeCompile
 *
 * 运行:
 * ./examples/tars-native-server/build/native/nativeCompile/tars-native-server
 */
public class NativeServerMain {

    private static final String HOST = "0.0.0.0";
    private static final int PORT = 18601;

    public static void main(String[] args) throws Exception {
        System.out.println("========================================");
        System.out.println("Tars Native Server Starting...");
        System.out.println("Host: " + HOST);
        System.out.println("Port: " + PORT);
        System.out.println("========================================");

        // 创建服务实现
        HelloServant servant = new HelloServantImpl();

        // 注册Servant方法信息到AnalystManager，供TarsDecoder/TarsEncoder使用
        String objName = "TestApp.HelloServer.HelloObj";
        AnalystManager.getInstance().registry(objName, HelloServant.class, objName);

        // 创建Endpoint
        Endpoint endpoint = new Endpoint("tcp", HOST, PORT, 60000, 0, 0, null);

        // 创建服务端配置
        ServantAdapterConfig config = new ServantAdapterConfig(endpoint, objName, null);

        // 创建处理器
        SimpleProcessor processor = new SimpleProcessor(servant);

        // 创建并启动服务
        ChannelHandler handler = new SimpleServerHandler(processor);
        TransporterServer server = new NettyTransporterServer(config, handler);
        server.bind();

        System.out.println("========================================");
        System.out.println("Tars Native Server Started!");
        System.out.println("Listening on " + HOST + ":" + PORT);
        System.out.println("Press Ctrl+C to stop");
        System.out.println("========================================");

        // 保持运行
        Thread.currentThread().join();
    }

    /**
     * 简单的请求处理器 - 不使用反射，直接调用方法
     */
    static class SimpleProcessor implements Processor {
        private final HelloServant servant;

        public SimpleProcessor(HelloServant servant) {
            this.servant = servant;
        }

        @Override
        public Response process(Request request, Channel channel) {
            TarsServantRequest tarsRequest = (TarsServantRequest) request;
            TarsServantResponse response = new TarsServantResponse(tarsRequest.getRequestId());
            response.setRequestId(tarsRequest.getRequestId());
            response.setRequest(tarsRequest);
            response.setVersion(tarsRequest.getVersion());
            response.setPacketType(tarsRequest.getPacketType());
            response.setMessageType(tarsRequest.getMessageType());
            response.setCharsetName(Charset.forName(tarsRequest.getCharsetName()));

            try {
                String methodName = tarsRequest.getFunctionName();
                Object[] args = tarsRequest.getMethodParameters();

                System.out.println("Received request: " + methodName + ", args: " + java.util.Arrays.toString(args));

                Object result = null;
                // 直接调用方法，避免反射
                switch (methodName) {
                    case "hello":
                        String name = args != null && args.length > 0 ? (String) args[0] : "World";
                        result = servant.hello(name);
                        break;
                    case "add":
                        int a = args != null && args.length > 0 ? ((Number) args[0]).intValue() : 0;
                        int b = args != null && args.length > 1 ? ((Number) args[1]).intValue() : 0;
                        result = servant.add(a, b);
                        break;
                    default:
                        response.setRet(TarsHelper.SERVERNOFUNCERR);
                        response.setRemark("Method not found: " + methodName);
                        return response;
                }

                response.setResult(result);
                response.setRet(TarsHelper.SERVERSUCCESS);
                System.out.println("Response: " + result);
            } catch (Exception e) {
                e.printStackTrace();
                response.setRet(TarsHelper.SERVERUNKNOWNERR);
                response.setRemark(e.getMessage());
            }

            return response;
        }

        @Override
        public void overload(Request request, Channel clientChannel) {
            // 过载处理
            TarsServantRequest tarsRequest = (TarsServantRequest) request;
            TarsServantResponse response = new TarsServantResponse(tarsRequest.getRequestId());
            response.setRequestId(tarsRequest.getRequestId());
            response.setRet(TarsHelper.SERVEROVERLOAD);
            response.setRemark("Server overload");
            if (clientChannel.isWritable()) {
                clientChannel.writeAndFlush(response);
            }
        }
    }

    /**
     * 简单的服务端Channel处理器
     */
    static class SimpleServerHandler implements ChannelHandler {
        private final Processor processor;

        public SimpleServerHandler(Processor processor) {
            this.processor = processor;
        }

        @Override
        public void connected(Channel channel) {
            System.out.println("Client connected: " + channel.remoteAddress());
        }

        @Override
        public void disconnected(Channel channel) {
            System.out.println("Client disconnected: " + channel.remoteAddress());
        }

        @Override
        public void send(Channel channel, Object message) {
        }

        @Override
        public void received(Channel channel, Object message) {
            Response response = processor.process((Request) message, channel);
            if (!response.isAsyncMode() && channel.isWritable()) {
                channel.writeAndFlush(response);
            }
        }

        @Override
        public void caught(Channel channel, Throwable exception) {
            exception.printStackTrace();
        }

        @Override
        public void destroy() {
        }
    }
}
