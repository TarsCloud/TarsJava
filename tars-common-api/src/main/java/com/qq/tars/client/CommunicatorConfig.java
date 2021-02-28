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

package com.qq.tars.client;

import com.qq.tars.common.util.Config;
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class CommunicatorConfig {

    private String locator = "";
    private int syncInvokeTimeout = 3000;
    private int asyncInvokeTimeout = 3000;
    private int refreshEndpointInterval = 60000;
    private int reportInterval = 60000;

    private String stat = Constants.default_stat;
    private String property = null;

    private int sampleRate = 0;
    private int maxSampleCount = 0;

    private int sendThread = 1;
    private int recvThread = 1;
    private int asyncThread = 1;
    private String moduleName = Constants.DEFAULT_MODULE_NAME;

    private boolean enableSet = false;
    private String setDivision = null;
    private String setName;
    private String setArea;
    private String setID;

    private int connections = Constants.DEFAULT_CONNECTION;
    private int connectTimeout = Constants.DEFAULT_CONNECTION_TIMEOUT;
    private int corePoolSize = Constants.DEFAULT_CORE_POOL_SIZE;
    private int maxPoolSize = Constants.DEFAULT_MAX_POOL_SIZE;
    private int keepAliveTime = Constants.DEFAULT_KEEPALIVE_TIME;
    private int queueSize = Constants.DEFAULT_QUEUE_SIZE;
    private String charsetName = Constants.DEFAULT_CHARSET.name();

    private String logPath;
    private String logLevel = "INFO";
    private String dataPath;

    public CommunicatorConfig() {
    }

    public static CommunicatorConfig load(String confFile) throws FileNotFoundException, IOException {
        CommunicatorConfig cfg = new CommunicatorConfig();
        cfg.load(Config.parseFile(confFile));
        return cfg;
    }

    public static CommunicatorConfig getDefault() {
        return new CommunicatorConfig();
    }

    public CommunicatorConfig load(Config conf) {
        locator = conf.get("/tars/application/client<locator>");
        logPath = conf.get("/tars/application/client<logpath>", null);
        logLevel = conf.get("/tars/application/client<loglevel>", "INFO");
        dataPath = conf.get("/tars/application/client<cdatapath>", null);
        syncInvokeTimeout = conf.getInt("/tars/application/client<sync-invoke-timeout>", 3000);
        asyncInvokeTimeout = conf.getInt("/tars/application/client<async-invoke-timeout>", 3000);
        refreshEndpointInterval = conf.getInt("/tars/application/client<refresh-endpoint-interval>", 60000);
        stat = conf.get("/tars/application/client<stat>");
        property = conf.get("/tars/application/client<property>");
        reportInterval = conf.getInt("/tars/application/client<report-interval>", 60000);
        sampleRate = conf.getInt("/tars/application/client<sample-rate>", 1000);
        maxSampleCount = conf.getInt("/tars/application/client<max-sample-count>", 100);
        sendThread = conf.getInt("/tars/application/client<sendthread>", 1);
        recvThread = conf.getInt("/tars/application/client<recvthread>", 1);
        asyncThread = conf.getInt("/tars/application/client<asyncthread>", 1);
        moduleName = conf.get("/tars/application/client<modulename>", Constants.DEFAULT_MODULE_NAME);
        String enableSetStr = conf.get("/tars/application<enableset>");
        setDivision = conf.get("/tars/application<setdivision>");
        if ("Y".equalsIgnoreCase(enableSetStr)) {
            enableSet = true;
        } else {
            enableSet = false;
            setDivision = null;
        }
        if (enableSet && setDivision != null) {
            this.setSetDivision(setDivision);
        }
        connections = conf.getInt("/tars/application/client<connections>", Constants.DEFAULT_CONNECTION);
        connectTimeout = conf.getInt("/tars/application/client<connect-timeout>", Constants.DEFAULT_CONNECTION_TIMEOUT);
        corePoolSize = conf.getInt("/tars/application/client<corepoolsize>", Constants.DEFAULT_CORE_POOL_SIZE);
        maxPoolSize = conf.getInt("/tars/application/client<maxpoolsize>", Constants.DEFAULT_MAX_POOL_SIZE);
        keepAliveTime = conf.getInt("/tars/application/client<keepalivetime>", Constants.DEFAULT_KEEPALIVE_TIME);
        queueSize = conf.getInt("/tars/application/client<queuesize>", Constants.DEFAULT_QUEUE_SIZE);
        charsetName = conf.get("/tars/application/client<charsetname>", Constants.DEFAULT_CHARSET.name());
        return this;
    }

    public String getLocator() {
        return locator;
    }

    public CommunicatorConfig setLocator(String locator) {
        this.locator = locator;
        return this;
    }

    public int getSyncInvokeTimeout() {
        return syncInvokeTimeout;
    }

    public CommunicatorConfig setSyncInvokeTimeout(int syncInvokeTimeout) {
        this.syncInvokeTimeout = syncInvokeTimeout;
        return this;
    }

    public int getAsyncInvokeTimeout() {
        return asyncInvokeTimeout;
    }

    public CommunicatorConfig setAsyncInvokeTimeout(int asyncInvokeTimeout) {
        this.asyncInvokeTimeout = asyncInvokeTimeout;
        return this;
    }

    public int getRefreshEndpointInterval() {
        return refreshEndpointInterval;
    }

    public CommunicatorConfig setRefreshEndpointInterval(int refreshEndpointInterval) {
        this.refreshEndpointInterval = refreshEndpointInterval;
        return this;
    }

    public String getStat() {
        return stat;
    }

    public CommunicatorConfig setStat(String stat) {
        this.stat = stat;
        return this;
    }

    public String getProperty() {
        return property;
    }

    public CommunicatorConfig setProperty(String property) {
        this.property = property;
        return this;
    }

    public int getReportInterval() {
        return reportInterval;
    }

    public CommunicatorConfig setReportInterval(int reportInterval) {
        this.reportInterval = reportInterval;
        return this;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public CommunicatorConfig setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public int getMaxSampleCount() {
        return maxSampleCount;
    }

    public CommunicatorConfig setMaxSampleCount(int maxSampleCount) {
        this.maxSampleCount = maxSampleCount;
        return this;
    }

    @Deprecated
    public int getSendThread() {
        return sendThread;
    }

    @Deprecated
    public CommunicatorConfig setSendThread(int sendThread) {
        this.sendThread = sendThread;
        return this;
    }

    @Deprecated
    public int getRecvThread() {
        return recvThread;
    }

    @Deprecated
    public CommunicatorConfig setRecvThread(int recvThread) {
        this.recvThread = recvThread;
        return this;
    }

    @Deprecated
    public int getAsyncThread() {
        return asyncThread;
    }

    @Deprecated
    public CommunicatorConfig setAsyncThread(int asyncThread) {
        this.asyncThread = asyncThread;
        return this;
    }

    public String getModuleName() {
        return moduleName;
    }

    public CommunicatorConfig setModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public boolean isEnableSet() {
        return enableSet;
    }

    public CommunicatorConfig setEnableSet(boolean enableSet) {
        this.enableSet = enableSet;
        return this;
    }

    public String getSetDivision() {
        return setDivision;
    }

    public CommunicatorConfig setSetDivision(String setDivision) {
        this.setDivision = setDivision;
        if (StringUtils.isNotEmpty(setDivision)) {
            String[] tmp = StringUtils.split(setDivision, ".");
            if (tmp != null && tmp.length == 3) {
                setName = tmp[0];
                setArea = tmp[1];
                setID = tmp[2];
                enableSet = true;
            } else {
                setName = "";
                setArea = "";
                setID = "";
                enableSet = false;
            }
        }
        return this;
    }

    public String getSetName() {
        return setName;
    }

    public String getSetArea() {
        return setArea;
    }

    public String getSetID() {
        return setID;
    }

    public int getConnections() {
        return connections;
    }

    public CommunicatorConfig setConnections(int connections) {
        this.connections = connections;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public CommunicatorConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public CommunicatorConfig setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public CommunicatorConfig setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public CommunicatorConfig setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public CommunicatorConfig setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    public String getLogPath() {
        return logPath;
    }

    public CommunicatorConfig setLogPath(String logPath) {
        this.logPath = logPath;
        return this;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public CommunicatorConfig setLogLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public String getDataPath() {
        return dataPath;
    }

    public CommunicatorConfig setDataPath(String dataPath) {
        this.dataPath = dataPath;
        return this;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public CommunicatorConfig setCharsetName(String charsetName) {
        this.charsetName = charsetName;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((locator == null) ? 0 : locator.hashCode());
        result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CommunicatorConfig other = (CommunicatorConfig) obj;
        if (!Objects.equals(this.locator, other.locator)) {
            return false;
        }
        return Objects.equals(this.moduleName, other.moduleName);
    }

    @Override
    public String toString() {
        return "CommunicatorConfig{" +
                "locator='" + locator + '\'' +
                ", syncInvokeTimeout=" + syncInvokeTimeout +
                ", asyncInvokeTimeout=" + asyncInvokeTimeout +
                ", refreshEndpointInterval=" + refreshEndpointInterval +
                ", reportInterval=" + reportInterval +
                ", stat='" + stat + '\'' +
                ", property='" + property + '\'' +
                ", sampleRate=" + sampleRate +
                ", maxSampleCount=" + maxSampleCount +
                ", sendThread=" + sendThread +
                ", recvThread=" + recvThread +
                ", asyncThread=" + asyncThread +
                ", moduleName='" + moduleName + '\'' +
                ", enableSet=" + enableSet +
                ", setDivision='" + setDivision + '\'' +
                ", setName='" + setName + '\'' +
                ", setArea='" + setArea + '\'' +
                ", setID='" + setID + '\'' +
                ", connections=" + connections +
                ", connectTimeout=" + connectTimeout +
                ", corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", keepAliveTime=" + keepAliveTime +
                ", queueSize=" + queueSize +
                ", charsetName='" + charsetName + '\'' +
                ", logPath='" + logPath + '\'' +
                ", logLevel='" + logLevel + '\'' +
                ", dataPath='" + dataPath + '\'' +
                '}';
    }
}
