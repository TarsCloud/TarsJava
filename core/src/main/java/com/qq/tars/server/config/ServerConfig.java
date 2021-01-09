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

package com.qq.tars.server.config;

import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.common.support.Endpoint;
import com.qq.tars.common.util.Config;
import com.qq.tars.common.util.Constants;
import com.qq.tars.support.om.OmConstants;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;

public class ServerConfig {
    private String application;
    private String serverName;
    private Endpoint local;
    private String node;
    private String basePath;
    private String config;
    private String notify;
    private String log;
    private String logPath;
    private String logLevel;
    private int logRate = 5;
    private String dataPath;
    private String localIP;
    private float sampleRate = 0;
    private String sampleType;
    private String sampleAddress;
    private String sampleEncoding;
    private int sessionTimeOut = 120000;
    private int sessionCheckInterval = 60000;
    private boolean tcpNoDelay = false;
    private int udpBufferSize = 4096;
    private int logMaxHistry = 15;

    public int getLogMaxHistry() {
        return logMaxHistry;
    }

    public void setLogMaxHistry(int logMaxHistry) {
        this.logMaxHistry = logMaxHistry;
    }

    private Charset charsetName = StandardCharsets.UTF_8;
    private LinkedHashMap<String, ServantAdapterConfig> servantAdapterConfMap;
    private CommunicatorConfig communicatorConfig;


    public ServerConfig load(Config conf) {
        application = conf.get("/tars/application/server<app>", "UNKNOWN");
        serverName = conf.get("/tars/application/server<server>", null);
        String localStr = conf.get("/tars/application/server<local>");
        local = localStr == null || localStr.length() <= 0 ? null : Endpoint
                .parseString(localStr);
        node = conf.get("/tars/application/server<node>");
        basePath = conf.get("/tars/application/server<basepath>");
        dataPath = conf.get("/tars/application/server<datapath>");
        logMaxHistry = conf.getInt("/tars/application/server<maxHistory>", 15);

        charsetName = Charset.forName(conf.get("/tars/application/server<charsetname>", Constants.DEFAULT_CHARSET_STRING));

        config = conf.get("/tars/application/server<config>");
        notify = conf.get("/tars/application/server<notify>");

        log = conf.get("/tars/application/server<log>");
        logPath = conf.get("/tars/application/server<logpath>", null);
        logLevel = conf.get("/tars/application/server<loglevel>");
        logRate = conf.getInt("/tars/application/server<lograte>", 5);

        localIP = conf.get("/tars/application/server<localip>");

        sampleRate = Float.parseFloat(conf.get("/tars/application/server<samplerate>", "0"));
        sampleType = conf.get("/tars/application/server<sampletype>");
        sampleAddress = conf.get("/tars/application/server<sampleaddress>");
        sampleEncoding = conf.get("/tars/application/server<sampleencoding>", "json");

        sessionTimeOut = conf.getInt(
                "/tars/application/server<sessiontimeout>", 120000);
        sessionCheckInterval = conf.getInt(
                "/tars/application/server<sessioncheckinterval>", 60000);
        udpBufferSize = conf.getInt("/tars/application/server<udpbuffersize>",
                4096);
        tcpNoDelay = conf.getBool("/tars/application/server<tcpnodelay>", false);

        servantAdapterConfMap = new LinkedHashMap<>();
        List<String> adapterNameList = conf.getSubTags("/tars/application/server");
        if (adapterNameList != null) {
            for (String adapterName : adapterNameList) {
                ServantAdapterConfig config = ServantAdapterConfig.makeServantAdapterConfig(conf, adapterName, this);
                servantAdapterConfMap.put(config.getServant(), config);
            }
        }
        final ServantAdapterConfig adminServantAdapterConfig =
                ServantAdapterConfig.makeServantAdapterConfig(local,
                        String.format("%s.%s.%s", application, serverName, OmConstants.AdminServant),
                        this);
        servantAdapterConfMap.put(OmConstants.AdminServant, adminServantAdapterConfig);

        if (application != null && serverName != null && logPath != null) {
            logPath = logPath + File.separator + application + File.separator + serverName;
        }
        communicatorConfig = new CommunicatorConfig().load(conf);
        if (logPath != null) {
            communicatorConfig.setLogPath(logPath);
        }
        communicatorConfig.setLogLevel(logLevel);
        communicatorConfig.setDataPath(dataPath);
        return this;
    }

    public ServerConfig() {
        application = "UNKNOWN";
        serverName = "";
        String localStr = "";
        logMaxHistry = 15;

        charsetName = Constants.DEFAULT_CHARSET;
        logRate = 5;


        sessionTimeOut = 120000;
        sessionCheckInterval = 60000;
        udpBufferSize = 4096;
        tcpNoDelay =false;
        servantAdapterConfMap = new LinkedHashMap<>();
    }

    public String getApplication() {
        return application;
    }

    public ServerConfig setApplication(String application) {
        this.application = application;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public ServerConfig setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public Endpoint getLocal() {
        return local;
    }

    public ServerConfig setLocal(Endpoint local) {
        this.local = local;
        return this;
    }

    public String getNode() {
        return node;
    }

    public ServerConfig setNode(String node) {
        this.node = node;
        return this;
    }

    public String getBasePath() {
        return basePath;
    }

    public ServerConfig setBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public ServerConfig setConfig(String config) {
        this.config = config;
        return this;
    }

    public String getNotify() {
        return notify;
    }

    public ServerConfig setNotify(String notify) {
        this.notify = notify;
        return this;
    }

    public String getLog() {
        return log;
    }

    public ServerConfig setLog(String log) {
        this.log = log;
        return this;
    }

    public String getLogPath() {
        return logPath;
    }

    public ServerConfig setLogPath(String logPath) {
        this.logPath = logPath;
        return this;
    }

    public int getLogRate() {
        return logRate;
    }

    public ServerConfig setLogRate(int logRate) {
        this.logRate = logRate;
        return this;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public ServerConfig setLogLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public String getLocalIP() {
        return localIP;
    }

    public ServerConfig setLocalIP(String localIP) {
        this.localIP = localIP;
        return this;
    }

    public LinkedHashMap<String, ServantAdapterConfig> getServantAdapterConfMap() {
        return servantAdapterConfMap;
    }

    public ServerConfig setServantAdapterConfMap(
            LinkedHashMap<String, ServantAdapterConfig> servantAdapterConfMap) {
        this.servantAdapterConfMap = servantAdapterConfMap;
        return this;
    }

    public CommunicatorConfig getCommunicatorConfig() {
        return communicatorConfig;
    }

    public ServerConfig setCommunicatorConfig(
            CommunicatorConfig communicatorConfig) {
        this.communicatorConfig = communicatorConfig;
        return this;
    }

    public String getDataPath() {
        return dataPath;
    }

    public ServerConfig setDataPath(String dataPath) {
        this.dataPath = dataPath;
        return this;
    }

    public int getSessionTimeOut() {
        return sessionTimeOut;
    }

    public ServerConfig setSessionTimeOut(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
        return this;
    }

    public int getSessionCheckInterval() {
        return sessionCheckInterval;
    }

    public ServerConfig setSessionCheckInterval(int sessionCheckInterval) {
        this.sessionCheckInterval = sessionCheckInterval;
        return this;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public ServerConfig setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    public int getUdpBufferSize() {
        return udpBufferSize;
    }

    public ServerConfig setUdpBufferSize(int udpBufferSize) {
        this.udpBufferSize = udpBufferSize;
        return this;
    }

    public Charset getCharsetName() {
        return charsetName;
    }

    public ServerConfig setCharsetName(Charset charsetName) {
        this.charsetName = charsetName;
        return this;
    }

    public int getLocalPort() {
        return this.local.port();
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getSampleAddress() {
        return sampleAddress;
    }

    public void setSampleAddress(String sampleAddress) {
        this.sampleAddress = sampleAddress;
    }

    public String getSampleEncoding() {
        return sampleEncoding;
    }

    public void setSampleEncoding(String sampleEncoding) {
        this.sampleEncoding = sampleEncoding;
    }


    @Override
    public String toString() {
        return "ServerConfig{" +
                "application='" + application + '\'' +
                ", serverName='" + serverName + '\'' +
                ", local=" + local +
                ", node='" + node + '\'' +
                ", basePath='" + basePath + '\'' +
                ", config='" + config + '\'' +
                ", notify='" + notify + '\'' +
                ", log='" + log + '\'' +
                ", logPath='" + logPath + '\'' +
                ", logLevel='" + logLevel + '\'' +
                ", logRate=" + logRate +
                ", dataPath='" + dataPath + '\'' +
                ", localIP='" + localIP + '\'' +
                ", sampleRate=" + sampleRate +
                ", sampleType='" + sampleType + '\'' +
                ", sampleAddress='" + sampleAddress + '\'' +
                ", sampleEncoding='" + sampleEncoding + '\'' +
                ", sessionTimeOut=" + sessionTimeOut +
                ", sessionCheckInterval=" + sessionCheckInterval +
                ", tcpNoDelay=" + tcpNoDelay +
                ", udpBufferSize=" + udpBufferSize +
                ", logMaxHistry=" + logMaxHistry +
                ", charsetName='" + charsetName + '\'' +
                ", servantAdapterConfMap=" + servantAdapterConfMap +
                ", communicatorConfig=" + communicatorConfig +
                '}';
    }
}
