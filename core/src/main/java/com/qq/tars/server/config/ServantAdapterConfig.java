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

package com.qq.tars.server.config;

import com.qq.tars.common.support.Endpoint;
import com.qq.tars.common.util.Config;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class ServantAdapterConfig {

    private final Endpoint endpoint;
    private final int maxConns;
    private final int queueCap;
    private final int queueTimeout;
    private final String servant;
    private final String protocol;
    private final int threads;
    private final String handleGroup;

    private ServerConfig serverConfig;

    public ServantAdapterConfig(Config conf, String adapterName, ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        final String path = "/tars/application/server/" + adapterName;
        endpoint = Endpoint.parseString(conf.get(path + "<endpoint>"));
        handleGroup = conf.get(path + "<handlegroup>", null);
        protocol = conf.get(path + "<protocol>", "tars");
        maxConns = conf.getInt(path + "<maxconns>", 128);
        queueCap = conf.getInt(path + "<queuecap>", 1024);
        queueTimeout = conf.getInt(path + "<queuetimeout>", 10000);
        servant = conf.get(path + "<servant>");
        threads = conf.getInt(path + "<threads>", 1);
    }



    public static ServantAdapterConfig makeServantAdapterConfig(Endpoint endpoint, String servantName, ServerConfig serverConfig) {
        return new ServantAdapterConfig(endpoint, servantName, serverConfig);
    }

    public static ServantAdapterConfig makeServantAdapterConfig(Config conf, String adapterName, ServerConfig serverConfig) {
        return new ServantAdapterConfig(conf, adapterName, serverConfig);
    }


    public ServantAdapterConfig(Endpoint endpoint, String servantName, ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.endpoint = endpoint;
        this.handleGroup = null;
        this.protocol = "tars";
        this.maxConns = 128;
        this.queueCap = 1024;
        this.queueTimeout = 10000;
        this.servant = servantName;
        this.threads = 2;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }


    public int getMaxConns() {
        return maxConns;
    }

    public int getQueueCap() {
        return queueCap;
    }

    public int getQueueTimeout() {
        return queueTimeout;
    }


    public String getServant() {
        return servant;
    }


    public int getThreads() {
        return threads;
    }


    public String getProtocol() {
        return protocol;
    }

    public String getHandleGroup() {
        return handleGroup;
    }


    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }


}
