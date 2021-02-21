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

import com.qq.tars.client.rpc.TransporterAbstractFactory;
import com.qq.tars.common.support.Endpoint;
import com.qq.tars.rpc.exc.TarsException;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.config.ServerConfig;

public class ServantAdapter implements Adapter {
    private final ServantAdapterConfig servantAdapterConfig;
    private ServantHomeSkeleton skeleton;

    public ServantAdapter(ServantAdapterConfig servantAdapterConfig) {
        this.servantAdapterConfig = servantAdapterConfig;
    }

    public void bind(AppService appService) {
        this.skeleton = (ServantHomeSkeleton) appService;
        bind();

    }

    public void bind() {
        Processor processor = createProcessor(this.servantAdapterConfig.getServerConfig());
        Endpoint endpoint = this.servantAdapterConfig.getEndpoint();
        if (endpoint.type().equals("tcp")) {
            System.out.println("[SERVER] server starting at " + endpoint + "...");
            TransporterAbstractFactory.getInstance().getTransporterFactory().getTransporterServer(servantAdapterConfig, processor).bind();
            System.out.println("[SERVER] server started at " + endpoint + "...");
        }
        //            System.out.println("[SERVER] server starting at " + endpoint + "...");
        //            DatagramChannel serverChannel = DatagramChannel.open();
        //            DatagramSocket socket = serverChannel.socket();
        //            socket.bind(new InetSocketAddress(endpoint.host(), endpoint.port()));
        //            serverChannel.configureBlocking(false);
        //            System.out.println("[SERVER] servant started at " + endpoint + "...");
    }

    public ServantAdapterConfig getServantAdapterConfig() {
        return servantAdapterConfig;
    }

    public ServantHomeSkeleton getSkeleton() {
        return skeleton;
    }

    private Processor createProcessor(ServerConfig serverCfg) throws TarsException {
        return new TarsServantProcessor();
    }

    public void stop() {
    }
}
