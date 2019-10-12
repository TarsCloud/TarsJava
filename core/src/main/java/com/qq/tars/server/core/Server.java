/**
 * Tencent is pleased to support the open source community by making Tars available.
 * <p>
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 * <p>
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * https://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.qq.tars.server.core;

import com.qq.tars.client.Communicator;
import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.CommunicatorFactory;
import com.qq.tars.common.util.BeanAccessor;
import com.qq.tars.net.core.SessionManager;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.server.ha.ConnectionSessionListener;
import com.qq.tars.support.om.OmConstants;
import com.qq.tars.support.om.OmServiceMngr;

import java.io.IOException;
import java.util.Map.Entry;

public class Server {

    private AppContext appContext = null;
    private ServerConfig serverConfig;
    private static final Server INSTANCE = new Server();

    private Server() {
        System.out.println("[TARS] start server construction");
        loadServerConfig();
        initCommunicator();
        startManagerService();
    }

    public static Server getInstance() {
        return INSTANCE;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void startUp(AppContext appContext) {
        try {
            startAppContext(appContext);
            startSessionManager();
            registerServerHook();
            System.out.println("[SERVER] server is ready...");
        } catch (Throwable ex) {
            System.out.println("[SERVER] failed to start server...");
            ex.printStackTrace();
            System.out.close();
            System.err.close();
            System.exit(-1);
        }
    }

    private void startAppContext(AppContext appContext) throws Exception {
        AppContextManager.getInstance().setAppContext(appContext);
        this.appContext = appContext;
        appContext.init();
    }

    private void startManagerService() {
        OmServiceMngr.getInstance().initAndStartOmService();
    }

    private void initCommunicator() {
        CommunicatorConfig config = ConfigurationManager.getInstance().getServerConfig().getCommunicatorConfig();
        Communicator communicator = CommunicatorFactory.getInstance().getCommunicator(config);
        BeanAccessor.setBeanValue(CommunicatorFactory.getInstance(), "communicator", communicator);
    }

    private void loadServerConfig() {
        try {
            ConfigurationManager configurationManager = ConfigurationManager.getInstance();
            if (configurationManager.getServerConfig() == null) {
                configurationManager.init();
            }
            ServerConfig cfg = configurationManager.getServerConfig();
            System.setProperty("com.qq.nami.server.udp.bufferSize", String.valueOf(cfg.getUdpBufferSize()));
            System.setProperty("server.root", cfg.getBasePath());
            this.serverConfig = cfg;
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            System.err.println("The exception occurred at load server config");
            System.exit(2);
        }
    }

    private void startSessionManager() throws IOException {
        SessionManager sessionManager = SessionManager.getSessionManager();
        sessionManager.setTimeout(serverConfig.getSessionTimeOut());
        sessionManager.setCheckInterval(serverConfig.getSessionCheckInterval());

        int connCount = 0;
        for (Entry<String, ServantAdapterConfig> adapterConfigEntry : ConfigurationManager.getInstance().getServerConfig().getServantAdapterConfMap().entrySet()) {
            if (OmConstants.AdminServant.equals(adapterConfigEntry.getKey())) {
                continue;
            }
            connCount += adapterConfigEntry.getValue().getMaxConns();
        }
        ConnectionSessionListener sessionListener = new ConnectionSessionListener(connCount);
        sessionManager.addSessionListener(sessionListener);

        sessionManager.start();
    }

    private void registerServerHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (appContext != null) {
                    appContext.stop();
                }
            } catch (Exception ex) {
                System.err.println("The exception occurred at stopping server...");
            }
        }));
    }
}
