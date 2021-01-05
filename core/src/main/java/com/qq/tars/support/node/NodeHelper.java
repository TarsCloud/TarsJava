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

package com.qq.tars.support.node;

import com.qq.tars.client.Communicator;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.support.log.LoggerFactory;
import com.qq.tars.support.node.prx.ServerFPrx;
import com.qq.tars.support.node.prx.ServerInfo;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class NodeHelper {
    private static final Logger omLogger = LoggerFactory.getLogger("omLogger");

    private static final NodeHelper Instance = new NodeHelper();
    private Communicator communicator;
    private ServerInfo si;

    private NodeHelper() {
    }

    public static NodeHelper getInstance() {
        return Instance;
    }

    public void setNodeInfo(Communicator comm, String app, String server) {
        si = new ServerInfo(app, server, getPid(), null);
        communicator = comm;
    }

    public void keepAlive() {
        try {
            if (communicator == null) {
                return;
            }
            String node = ConfigurationManager.getInstance().getServerConfig().getNode();
            if (StringUtils.isEmpty(node)) {
                return;
            }
            ServerFPrx nodePrx = communicator.stringToProxy(ServerFPrx.class, node);
            nodePrx.promise_keepAlive(si);
        } catch (Throwable t) {
            omLogger.error("NodeHelper|keepAlive|error", t);
        }
    }

    public void reportVersion(String version) {
        try {
            if (communicator == null) {
                return;
            }
            String node = ConfigurationManager.getInstance().getServerConfig().getNode();
            if (StringUtils.isEmpty(node)) {
                return;
            }
            ServerFPrx nodePrx = communicator.stringToProxy(ServerFPrx.class, node);
            nodePrx.promise_reportVersion(si.application, si.serverName, version);
        } catch (Throwable t) {
            omLogger.error("NodeHelper|reportVersion|error", t);
        }
    }

    private int getPid() {
        RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
        String name = rmxb.getName();
        int pid = -1;
        try {
            pid = Integer.parseInt(name.split("@")[0]);
        } catch (Throwable t) {
        }
        return pid;
    }
}
