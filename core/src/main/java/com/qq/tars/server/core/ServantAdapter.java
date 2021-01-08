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

package com.qq.tars.server.core;

import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.client.rpc.NettyTransporter;
import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import com.qq.tars.common.support.Endpoint;
import com.qq.tars.rpc.exc.TarsException;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.config.ServerConfig;
import io.netty.channel.Channel;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class ServantAdapter implements Adapter {
    private final ServantAdapterConfig servantAdapterConfig;
    private ServantHomeSkeleton skeleton;
    public ServantAdapter(ServantAdapterConfig servantAdapterConfig) {
        this.servantAdapterConfig = servantAdapterConfig;
    }
    public void bind(AppService appService) throws IOException {
        this.skeleton = (ServantHomeSkeleton) appService;
        ServerConfig serverCfg = ConfigurationManager.getInstance().getServerConfig();
        Processor processor = createProcessor(serverCfg);
        Endpoint endpoint = this.servantAdapterConfig.getEndpoint();
        if (endpoint.type().equals("tcp")) {
            System.out.println("[SERVER] server starting at " + endpoint + "...");
            NettyTransporter.bind(servantAdapterConfig, new InnerDefaultHandler(processor));
            System.out.println("[SERVER] server started at " + endpoint + "...");
        } else if (endpoint.type().equals("udp")) {
//            System.out.println("[SERVER] server starting at " + endpoint + "...");
//            DatagramChannel serverChannel = DatagramChannel.open();
//            DatagramSocket socket = serverChannel.socket();
//            socket.bind(new InetSocketAddress(endpoint.host(), endpoint.port()));
//            serverChannel.configureBlocking(false);
//            System.out.println("[SERVER] servant started at " + endpoint + "...");
        }
    }


    public ServantAdapterConfig getServantAdapterConfig() {
        return servantAdapterConfig;
    }

    public ServantHomeSkeleton getSkeleton() {
        return skeleton;
    }

    private Processor createProcessor(ServerConfig serverCfg) throws TarsException {
        Processor processor = null;
        Class<? extends Processor> processorClass = skeleton.getProcessorClass();
        if (processorClass == null) {
            return new TarsServantProcessor();
        }
        if (processorClass != null) {
            Constructor<? extends Processor> constructor;
            try {
                constructor = processorClass.getConstructor(new Class[]{ServantAdapter.class});
                processor = constructor.newInstance(this);
            } catch (Exception e) {
                throw new TarsException("error occurred on create codec, codec=" + processorClass.getName());
            }
        }
        return processor;
    }


    private static class InnerDefaultHandler implements ChannelHandler {

        public final Processor processor;

        public InnerDefaultHandler(Processor processor) {
            this.processor = processor;
        }

        @Override
        public void connected(Channel channel) {

        }

        @Override
        public void disconnected(Channel channel) {

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

        }

        @Override
        public void destroy() {

        }
    }


    public void stop() {
    }
}
