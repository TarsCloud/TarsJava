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
import com.qq.tars.client.CommunicatorFactory;
import com.qq.tars.common.ClientVersion;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.support.config.ConfigHelper;
import com.qq.tars.support.node.NodeHelper;
import com.qq.tars.support.notify.NotifyHelper;
import com.qq.tars.support.property.CommonPropertyPolicy;
import com.qq.tars.support.property.JvmPropertyPolicy.GCNumCount;
import com.qq.tars.support.property.JvmPropertyPolicy.GCTimeSum;
import com.qq.tars.support.property.JvmPropertyPolicy.MemoryHeapCommittedAvg;
import com.qq.tars.support.property.JvmPropertyPolicy.MemoryHeapMaxAvg;
import com.qq.tars.support.property.JvmPropertyPolicy.MemoryHeapUsedAvg;
import com.qq.tars.support.property.JvmPropertyPolicy.ThreadNumAvg;
import com.qq.tars.support.property.PropertyReportHelper;
import com.qq.tars.support.property.PropertyReportHelper.Policy;
import com.qq.tars.support.trace.TarsTraceZipkinConfiguration;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class OmServiceMngr {

    private static final OmServiceMngr Instance = new OmServiceMngr();

    public static OmServiceMngr getInstance() {
        return Instance;
    }

    public synchronized void initAndStartOmService() {
        Communicator communicator = CommunicatorFactory.getInstance().getCommunicator();
        String app = ConfigurationManager.getInstance().getServerConfig().getApplication();
        String serverName = ConfigurationManager.getInstance().getServerConfig().getServerName();
        String basePath = ConfigurationManager.getInstance().getServerConfig().getBasePath();
        String modualName = ConfigurationManager.getInstance().getServerConfig().getCommunicatorConfig().getModuleName();

        ConfigHelper.getInstance().setConfigInfo(communicator, app, serverName, basePath);
        NodeHelper.getInstance().setNodeInfo(communicator, app, serverName);
        NotifyHelper.getInstance().setNotifyInfo(communicator, app, serverName);
        PropertyReportHelper.getInstance().init(communicator, modualName);
        NodeHelper.getInstance().reportVersion(ClientVersion.getVersion());

        Policy avgPolicy = new CommonPropertyPolicy.Avg();
        Policy maxPolicy = new CommonPropertyPolicy.Max();
        PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropWaitTime, avgPolicy, maxPolicy);

        PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropHeapUsed, new MemoryHeapUsedAvg());
        PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropHeapCommitted, new MemoryHeapCommittedAvg());
        PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropHeapMax, new MemoryHeapMaxAvg());
        PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropThreadCount, new ThreadNumAvg());
        for (GarbageCollectorMXBean gcMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropGcCount + gcMXBean.getName(), new GCNumCount(gcMXBean.getName()));
            PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropGcTime + gcMXBean.getName(), new GCTimeSum(gcMXBean.getName()));
        }

        ServerStatHelper.getInstance().init(communicator);
        TarsTraceZipkinConfiguration.getInstance().init();
        ScheduledServiceMngr.getInstance().start();
    }

    public void reportWaitingTimeProperty(int value) {
        PropertyReportHelper.getInstance().reportPropertyValue(OmConstants.PropWaitTime, value);
    }
}
