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

package com.qq.tars.support.om;

import com.qq.tars.client.Communicator;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.support.log.LoggerFactory;
import com.qq.tars.support.stat.InvokeStatHelper;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerStatHelper {
    private static final Logger omLogger = LoggerFactory.getOmLogger();

    private static final ServerStatHelper instance = new ServerStatHelper();
    private final AtomicBoolean inited = new AtomicBoolean(false);

    private volatile Communicator communicator;

    private ServerStatHelper() {
    }

    public static ServerStatHelper getInstance() {
        return instance;
    }

    public void init(Communicator communicator) {
        if (inited.compareAndSet(false, true)) {
            this.communicator = communicator;
        }
    }

    public void report() {
        try {
            ServerConfig serverConfig = ConfigurationManager.getInstance().getServerConfig();
            LinkedHashMap<String, ServantAdapterConfig> adapterMap = serverConfig.getServantAdapterConfMap();
            if (adapterMap == null || adapterMap.isEmpty()) {
                return;
            }

            for (java.util.Map.Entry<String, ServantAdapterConfig> adapterEntry : adapterMap.entrySet()) {
                if (OmConstants.AdminServant.equals(adapterEntry.getKey())) {
                    continue;
                }

                ServantAdapterConfig servantCfg = adapterEntry.getValue();
                communicator.getStatHelper().report(InvokeStatHelper.getInstance().getProxyStat(servantCfg.getServant()), false);
            }
        } catch (Exception e) {
            omLogger.error("ServerStatHelper|ReportThread error", e);
        }
    }
}
