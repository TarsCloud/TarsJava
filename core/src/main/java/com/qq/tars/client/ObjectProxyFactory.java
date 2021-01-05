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

import com.qq.tars.client.rpc.loadbalance.DefaultLoadBalance;
import com.qq.tars.client.rpc.tars.TarsProtocolInvoker;
import com.qq.tars.client.support.ServantCacheManager;
import com.qq.tars.client.util.ParseTools;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.protocol.annotation.Servant;
import com.qq.tars.protocol.annotation.ServantCodec;
import com.qq.tars.register.RegisterManager;
import com.qq.tars.rpc.common.LoadBalance;
import com.qq.tars.rpc.common.ProtocolInvoker;
import com.qq.tars.rpc.exc.ClientException;
import com.qq.tars.rpc.exc.CommunicatorConfigException;
import com.qq.tars.rpc.protocol.Codec;
import com.qq.tars.rpc.protocol.tars.TarsClientCodec;
import com.qq.tars.support.log.LoggerFactory;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;

class ObjectProxyFactory {
    private static final Logger logger = LoggerFactory.getClientLogger();


    private final Communicator communicator;

    public ObjectProxyFactory(Communicator communicator) {
        this.communicator = communicator;
    }

    public <T> ObjectProxy<T> getObjectProxy(Class<T> api, String objName, String setDivision, ServantProxyConfig servantProxyConfig,
                                             LoadBalance<T> loadBalance, ProtocolInvoker<T> protocolInvoker) throws ClientException {
        if (servantProxyConfig == null) {
            servantProxyConfig = createServantProxyConfig(objName, setDivision);
        } else {
            servantProxyConfig.setCommunicatorId(communicator.getId());
            servantProxyConfig.setModuleName(communicator.getCommunicatorConfig().getModuleName(), communicator.getCommunicatorConfig().isEnableSet(), communicator.getCommunicatorConfig().getSetDivision());
            servantProxyConfig.setLocator(communicator.getCommunicatorConfig().getLocator());
            addSetDivisionInfo(servantProxyConfig, setDivision);
            servantProxyConfig.setRefreshInterval(communicator.getCommunicatorConfig().getRefreshEndpointInterval());
            servantProxyConfig.setReportInterval(communicator.getCommunicatorConfig().getReportInterval());
        }

        updateServantEndpoints(servantProxyConfig);

        if (loadBalance == null) {
            loadBalance = createLoadBalance(servantProxyConfig);
        }

        if (protocolInvoker == null) {
            protocolInvoker = createProtocolInvoker(api, servantProxyConfig);
        }
        return new ObjectProxy<T>(api, servantProxyConfig, loadBalance, protocolInvoker, communicator);
    }

    private <T> ProtocolInvoker<T> createProtocolInvoker(Class<T> api,
                                                         ServantProxyConfig servantProxyConfig) throws ClientException {
        ProtocolInvoker<T> protocolInvoker = null;
        Codec codec = createCodec(api, servantProxyConfig);
        if (api.isAnnotationPresent(Servant.class)) {
            if (codec == null) {
                codec = new TarsClientCodec(servantProxyConfig.getCharsetName());
            }
            servantProxyConfig.setProtocol(codec.getProtocol());
            protocolInvoker = new TarsProtocolInvoker<T>(api, servantProxyConfig);
        } else {
            throw new ClientException(servantProxyConfig.getSimpleObjectName(), "unknown protocol servant invoker", null);
        }
        return protocolInvoker;
    }

    private <T> LoadBalance<T> createLoadBalance(ServantProxyConfig servantProxyConfig) {
        return new DefaultLoadBalance<T>(servantProxyConfig);
    }

    private <T> Codec createCodec(Class<T> api, ServantProxyConfig servantProxyConfig) throws ClientException {
        Codec codec = null;
        ServantCodec servantCodec = api.getAnnotation(ServantCodec.class);
        if (servantCodec != null) {
            Class<? extends Codec> codecClass = servantCodec.codec();
            if (codecClass != null) {
                Constructor<? extends Codec> constructor;
                try {
                    constructor = codecClass.getConstructor(new Class[]{String.class});
                    codec = constructor.newInstance(servantProxyConfig.getCharsetName());
                } catch (Exception e) {
                    throw new ClientException(servantProxyConfig.getSimpleObjectName(), "error occurred on create codec, codec=" + codecClass.getName(), e);
                }
            }
        }
        return codec;
    }

    private ServantProxyConfig createServantProxyConfig(String objName, String setDivision) throws CommunicatorConfigException {
        CommunicatorConfig communicatorConfig = communicator.getCommunicatorConfig();
        ServantProxyConfig cfg = new ServantProxyConfig(communicator.getId(), communicatorConfig.getLocator(), objName);
        cfg.setAsyncTimeout(communicatorConfig.getAsyncInvokeTimeout());
        cfg.setSyncTimeout(communicatorConfig.getSyncInvokeTimeout());
        addSetDivisionInfo(cfg, setDivision);
        cfg.setModuleName(communicatorConfig.getModuleName(), communicatorConfig.isEnableSet(), communicatorConfig.getSetDivision());
        cfg.setStat(communicatorConfig.getStat());
        cfg.setCharsetName(communicatorConfig.getCharsetName());
        cfg.setConnections(communicatorConfig.getConnections());
        cfg.setRefreshInterval(communicatorConfig.getRefreshEndpointInterval());
        cfg.setReportInterval(communicator.getCommunicatorConfig().getReportInterval());
        return cfg;
    }

    public void addSetDivisionInfo(ServantProxyConfig cfg, String setDivision) {
        CommunicatorConfig communicatorConfig = communicator.getCommunicatorConfig();
        if (StringUtils.isNotEmpty(communicatorConfig.getSetDivision()) && communicatorConfig.isEnableSet()) {
            cfg.setSetDivision(communicatorConfig.getSetDivision());
        }
        //Custom priority
        if (StringUtils.isNotEmpty(setDivision)) {
            cfg.setSetDivision(setDivision);
        }
    }

    private void updateServantEndpoints(ServantProxyConfig cfg) {
        CommunicatorConfig communicatorConfig = communicator.getCommunicatorConfig();

        String endpoints = null;
        if (!ParseTools.hasServerNode(cfg.getObjectName()) && !cfg.isDirectConnection() && !communicatorConfig.getLocator().startsWith(cfg.getSimpleObjectName())) {
            try {
                /** query server nodes from registerServer */
                if (RegisterManager.getInstance().getHandler() != null) {
                    endpoints = ParseTools.parse(RegisterManager.getInstance().getHandler().query(cfg.getSimpleObjectName()),
                            cfg.getSimpleObjectName());
                } else {
                    endpoints = communicator.getQueryHelper().getServerNodes(cfg);
                }
                if (StringUtils.isEmpty(endpoints)) {
                    throw new CommunicatorConfigException(cfg.getSimpleObjectName(), "servant node is empty on get by registry! communicator id=" + communicator.getId());
                }
                ServantCacheManager.getInstance().save(communicator.getId(), cfg.getSimpleObjectName(), endpoints, communicatorConfig.getDataPath());
            } catch (CommunicatorConfigException e) {
                /** If it fails, pull it from the local cache  file */
                endpoints = ServantCacheManager.getInstance().get(communicator.getId(), cfg.getSimpleObjectName(), communicatorConfig.getDataPath());
                logger.error(cfg.getSimpleObjectName() + " error occurred on get by registry, use by local cache=" + endpoints + "|" + e.getLocalizedMessage(), e);
            }

            if (StringUtils.isEmpty(endpoints)) {
                throw new CommunicatorConfigException(cfg.getSimpleObjectName(), "error occurred on create proxy, servant endpoint is empty! locator =" + communicatorConfig.getLocator() + "|communicator id=" + communicator.getId());
            }
            cfg.setObjectName(endpoints);
        }

        if (StringUtils.isEmpty(cfg.getObjectName())) {
            throw new CommunicatorConfigException(cfg.getSimpleObjectName(), "error occurred on create proxy, servant endpoint is empty!");
        }
    }
}
