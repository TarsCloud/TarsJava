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

package com.qq.tars.support.admin.impl;

import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.common.ClientVersion;
import com.qq.tars.common.logger.LoggerFactoryManager;
import com.qq.tars.common.util.DyeingKeyCache;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.support.admin.AdminFServant;
import com.qq.tars.support.admin.CommandHandler;
import com.qq.tars.support.admin.CustomCommandHelper;
import com.qq.tars.support.config.ConfigHelper;
import com.qq.tars.support.log.LoggerFactory;
import com.qq.tars.support.node.NodeHelper;
import com.qq.tars.support.notify.NotifyHelper;
import com.qq.tars.support.om.OmConstants;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Map.Entry;

public class AdminFServantImpl implements AdminFServant {
    private static final Logger omLogger = LoggerFactory.getOmLogger();

    private static final String CMD_LOAD_CONFIG = "tars.loadconfig";

    private static final String CMD_LOAD_LOCATOR = "tars.loadproperty";

    private static final String CMD_VIEW_VERSION = "tars.viewversion";

    private static final String CMD_SET_LEVEL = "tars.setloglevel";

    private static final String CMD_VIEW_CONN = "tars.connection";

    private static final String CMD_VIEW_STATUS = "tars.viewstatus";

    private static final String CMD_SET_DYEING = "tars.setdyeing";

    private static final String ADATER_CONN = "[adater:%sAdapter] [connections:%d]\n";

    @Override
    public void shutdown() {
        try {
            System.out.println(ConfigurationManager.getInstance().getServerConfig().getApplication() + "." + ConfigurationManager.getInstance().getServerConfig().getServerName() + " is stopped.");
            NotifyHelper.getInstance().syncReport("[alarm] server is stopped.");
        } catch (Exception e) {
            omLogger.error("shutdown error", e);
        }

        System.exit(0);
    }

    @Override
    public String notify(String command) {
        String params = "";
        String comm = command;
        if (command == null) {
            return "command is null";
        }
        int i = command.indexOf(" ");
        if (i != -1) {
            comm = command.substring(0, i);
            params = command.substring(i + 1);
        }

        StringBuilder result = new StringBuilder();
        result.append("\n");

        switch (comm) {
            case CMD_VIEW_STATUS:
                result.append(viewStatus()).append("\n");
                break;
            case CMD_VIEW_CONN:
                result.append(viewConn()).append("\n");
                break;
            case CMD_SET_LEVEL:
                result.append(setLoggerLevel(params)).append("\n");
                break;
            case CMD_LOAD_CONFIG:
                result.append(loadConfig(params)).append("\n");
                break;
            case CMD_LOAD_LOCATOR:
                result.append(loadLocator()).append("\n");
                break;
            case CMD_VIEW_VERSION:
                result.append(reportServerVersion()).append("\n");
                break;
            case CMD_SET_DYEING:
                result.append(loadDyeing(params)).append("\n");
                break;
            default:
                final CommandHandler handler = CustomCommandHelper.getInstance().getCommandHandler(comm);
                final String cmdName = comm;
                final String cmdParam = params;

                if (handler != null) {
                    Thread handleThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            handler.handle(cmdName, cmdParam);
                        }
                    });

                    handleThread.start();

                    result.append("custom command: cmdName=").append(cmdName).append(", params=").append(cmdParam).append("\n");
                } else {
                    result.append("invalid command.\n");
                }
                break;
        }

        NotifyHelper.getInstance().syncReport(command);
        return result.toString();
    }

    private String setLoggerLevel(String level) {
        String result = null;
        if (StringUtils.isEmpty(level)) {
            result = "set log level failed, level is empty";
        } else {
            level = level.trim().toUpperCase();
            LoggerFactoryManager.getInstance().getHandler().setLoggerLevel(Logger.ROOT_LOGGER_NAME, getLevelFromName(level));
            result = "set log level [" + level + "] ok";
        }

        return result;
    }

    public static Level getLevelFromName(String name) {
        for (Level level : Level.values()) {
            if (name.equals(level.name())) {
                return level;
            }
        }
        return Level.ERROR;
    }

    private String viewConn() {
        StringBuilder builder = new StringBuilder(128);
        String adminConnInfo = String.format(ADATER_CONN, "Admin", 128);
        builder.append(adminConnInfo);

        for (Entry<String, ServantAdapterConfig> adapterConfigEntry : ConfigurationManager.getInstance().getServerConfig().getServantAdapterConfMap().entrySet()) {
            if (OmConstants.AdminServant.equals(adapterConfigEntry.getKey())) {
                continue;
            }
            String adapterConnInfo = String.format(ADATER_CONN, adapterConfigEntry.getKey(), adapterConfigEntry.getValue().getMaxConns());
            builder.append(adapterConnInfo);
        }
        return builder.toString();
    }

    private String viewStatus() {
        StringBuilder builder = new StringBuilder(2048);
        String proxyConfigInfo = makeProxyConfigInfo();
        String serverConfigInfo = makeServerConfigInfo();
        String adapterConfigInfo = makeAdapterConfigInfo();

        builder.append(proxyConfigInfo).append("--------------------------------------------------\n");
        builder.append(serverConfigInfo).append("--------------------------------------------------\n");
        builder.append(adapterConfigInfo);
        return builder.toString();
    }

    private String makeAdapterConfigInfo() {
        StringBuilder builder = new StringBuilder(1024);

        builder.append("name \t AdminAdapter\n");
        builder.append("servant \t AdminObj\n");
        builder.append("endpoint \t tcp -h 127.0.0.1 -p ").append(ConfigurationManager.getInstance().getServerConfig().getLocalPort()).append(" -t 3000").append("\n");
        builder.append("maxconns \t 128\n");
        builder.append("queuecap \t 128\n");
        builder.append("queuetimeout \t 3000\n");
        builder.append("connections \t 128\n");
        builder.append("protocol \t tars\n");
        builder.append("handlegroup \t AdminAdapter\n");
        builder.append("handlethread  \t 1\n");

        builder.append("--------------------------------------------------\n");

        for (Entry<String, ServantAdapterConfig> adapterConfigEntry : ConfigurationManager.getInstance().getServerConfig().getServantAdapterConfMap().entrySet()) {
            if (OmConstants.AdminServant.equals(adapterConfigEntry.getKey())) {
                continue;
            }
            ServantAdapterConfig adapterConfig = adapterConfigEntry.getValue();

            builder.append("name \t").append(adapterConfigEntry.getKey()).append("Adapter\n");
            builder.append("servant \t").append(adapterConfig.getServant()).append("\n");
            builder.append("endpoint \t").append(adapterConfig.getEndpoint()).append("\n");
            builder.append("maxconns \t").append(adapterConfig.getMaxConns()).append("\n");
            builder.append("queuecap \t").append(adapterConfig.getQueueCap()).append("\n");
            builder.append("queuetimeout \t").append(adapterConfig.getQueueTimeout()).append("\n");
            builder.append("connections \t").append(adapterConfig.getMaxConns()).append("\n");
            builder.append("protocol \t").append(adapterConfig.getProtocol()).append("\n");
            builder.append("handlegroup \t").append(adapterConfigEntry.getKey()).append("\n");
            builder.append("handlethread  \t").append(adapterConfig.getThreads()).append("\n");

            builder.append("--------------------------------------------------\n");
        }

        return builder.toString();
    }

    private String makeServerConfigInfo() {
        StringBuilder builder = new StringBuilder(1024);
        ServerConfig serverConfig = ConfigurationManager.getInstance().getServerConfig();
        builder.append("[server config]:\n");
        builder.append("Application \t").append(serverConfig.getApplication()).append("\n");
        builder.append("ServerName \t").append(serverConfig.getServerName()).append("\n");
        builder.append("BasePath \t").append(serverConfig.getBasePath()).append("\n");
        builder.append("DataPath \t").append(serverConfig.getDataPath()).append("\n");
        builder.append("LocalIp \t").append(serverConfig.getLocalIP()).append("\n");
        builder.append("Local \ttcp -h 127.0.0.1 -p ").append(serverConfig.getLocalPort()).append(" -t 3000").append(
                "\n");
        builder.append("LogPath \t").append(serverConfig.getLogPath()).append("\n");
        builder.append("Log \t").append(serverConfig.getLog()).append("\n");
        builder.append("Node \t").append(serverConfig.getNode()).append("\n");
        builder.append("Config \t").append(serverConfig.getConfig()).append("\n");
        builder.append("Notify \t").append(serverConfig.getNotify()).append("\n");
        builder.append("logLevel \t").append(serverConfig.getLogLevel()).append("\n");

        return builder.toString();
    }

    private String makeProxyConfigInfo() {
        StringBuilder builder = new StringBuilder(1024);
        CommunicatorConfig commConfig = ConfigurationManager.getInstance().getServerConfig().getCommunicatorConfig();
        builder.append("[proxy config]:\n");
        builder.append("locator \t").append(commConfig.getLocator()).append("\n");
        builder.append("sync-invoke-timeout \t").append(commConfig.getSyncInvokeTimeout()).append("\n");
        builder.append("async-invoke-timeout \t").append(commConfig.getAsyncInvokeTimeout()).append("\n");
        builder.append("refresh-endpoint-interval \t").append(commConfig.getRefreshEndpointInterval()).append("\n");
        builder.append("stat \t").append(commConfig.getStat()).append("\n");
        builder.append("property \t").append(commConfig.getProperty()).append("\n");
        builder.append("report-interval \t").append(commConfig.getReportInterval()).append("\n");
        builder.append("sample-rate \t").append(commConfig.getSampleRate()).append("\n");
        builder.append("max-sample-count \t").append(commConfig.getMaxSampleCount()).append("\n");
        builder.append("recvthread \t").append(commConfig.getRecvThread()).append("\n");
        builder.append("asyncthread \t").append(commConfig.getAsyncThread()).append("\n");
        builder.append("modulename \t").append(commConfig.getModuleName()).append("\n");
        builder.append("enableset \t").append(commConfig.isEnableSet()).append("\n");
        builder.append("setdivision \t").append(commConfig.getSetDivision()).append("\n");

        return builder.toString();
    }

    private String reportServerVersion() {
        String version = ClientVersion.getVersion();
        NodeHelper.getInstance().reportVersion(version);
        return version;
    }

    private String loadLocator() {
        String result = null;

        try {
            result = "execute success.";
            omLogger.info("Reload locator success.");
        } catch (Exception e) {
            omLogger.error("Reload locator failed.", e);
            result = "execute exception: " + e.getMessage();
        }

        return result;
    }

    private String loadConfig(String params) {
        if (params == null) {
            return "invalid params";
        }
        String fileName = params.trim();
        if (StringUtils.isEmpty(fileName)) {
            return "invalid params.";
        }

        String result = null;
        try {
            boolean isSucc = ConfigHelper.getInstance().loadConfig(fileName);
            if (isSucc) {
                result = "execute success.";
            } else {
                result = "execute failed.";
            }
        } catch (Exception e) {
            result = "execute exception: " + e.getMessage();
        }

        return result;
    }

    private String loadDyeing(String params) {
        String result = null;
        if (params == null) {
            return "invalid params";
        }
        String[] paramArray = params.split(" ");
        if (paramArray.length < 2) {
            return "invalid params";
        }
        try {
            String routeKey = paramArray[0];
            String servantName = paramArray[1];
            String interfaceName = (paramArray.length >= 3) ? paramArray[2] : "DyeingAllFunctionsFromInterface";
            DyeingKeyCache.getInstance().set(servantName, interfaceName, routeKey);
            result = "execute success";
        } catch (Exception e) {
            result = "execute exception: " + e.getMessage();
        }
        return result;
    }

}
