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

import com.qq.tars.client.support.ClientPoolManager;
import com.qq.tars.common.support.ScheduledExecutorManager;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.rpc.common.LoadBalance;
import com.qq.tars.rpc.common.ProtocolInvoker;
import com.qq.tars.rpc.exc.CommunicatorConfigException;
import com.qq.tars.support.query.QueryHelper;
import com.qq.tars.support.query.prx.EndpointF;
import com.qq.tars.support.stat.StatHelper;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public final class Communicator {

    private volatile String id;
    private volatile CommunicatorConfig communicatorConfig;
    private volatile ThreadPoolExecutor threadPoolExecutor;
    private final ServantProxyFactory servantProxyFactory = new ServantProxyFactory(this);
    private final ObjectProxyFactory objectProxyFactory = new ObjectProxyFactory(this);

    private final QueryHelper queryHelper = new QueryHelper(this);
    private final StatHelper statHelper = new StatHelper(this);

    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicBoolean inited = new AtomicBoolean(false);

    Communicator(CommunicatorConfig config) {
        if (config != null) {
            this.initCommunicator(config);
        }
    }

    public <T> T stringToProxy(Class<T> clazz, String objName) throws CommunicatorConfigException {
        return stringToProxy(clazz, objName, null, null, null, null);
    }

    public <T> T stringToProxy(Class<T> clazz, String objName, String setDivision) throws CommunicatorConfigException {
        return stringToProxy(clazz, objName, setDivision, null, null, null);
    }

    public <T> T stringToProxy(Class<T> clazz, ServantProxyConfig servantProxyConfig)
            throws CommunicatorConfigException {
        return stringToProxy(clazz, servantProxyConfig.getObjectName(), servantProxyConfig.getSetDivision(),
                servantProxyConfig, null, null);
    }

    @Deprecated
    public <T> T stringToProxy(Class<T> clazz, ServantProxyConfig servantProxyConfig, LoadBalance<T> loadBalance)
            throws CommunicatorConfigException {
        return stringToProxy(clazz, servantProxyConfig.getObjectName(), servantProxyConfig.getSetDivision(),
                servantProxyConfig, loadBalance, null);
    }

    @SuppressWarnings("unchecked")
    private <T> T stringToProxy(Class<T> clazz, String objName, String setDivision,
            ServantProxyConfig servantProxyConfig, LoadBalance<T> loadBalance, ProtocolInvoker<T> protocolInvoker)
            throws CommunicatorConfigException {
        if (!inited.get()) {
            throw new CommunicatorConfigException("communicator uninitialized!");
        }
        return (T) getServantProxyFactory().getServantProxy(clazz, objName, setDivision, servantProxyConfig,
                loadBalance, protocolInvoker);
    }

    @Deprecated
    public void initialize(CommunicatorConfig config) throws CommunicatorConfigException {
        this.initCommunicator(config);
    }

    public void shutdown() {
        this.threadPoolExecutor.shutdownNow();
        ScheduledExecutorManager.getInstance().shutdownNow();
        for (Iterator<Object> it = servantProxyFactory.getProxyIterator(); it.hasNext();) {
            Object proxy = it.next();
            ((ObjectProxy) Proxy.getInvocationHandler(proxy)).destroy();
        }
    }

    private void initCommunicator(CommunicatorConfig config) throws CommunicatorConfigException {
        if (inited.get()) {
            return;
        }
        lock.lock();
        try {
            if (!inited.get()) {
                try {
                    if (StringUtils.isEmpty(config.getLocator())) {
                        this.id = UUID.randomUUID().toString().replaceAll("-", "");
                    } else {
                        this.id = UUID.nameUUIDFromBytes(config.getLocator().getBytes()).toString().replaceAll("-", "");
                    }
                    this.communicatorConfig = config;
                    this.threadPoolExecutor = ClientPoolManager.getClientThreadPoolExecutor(config);
                    inited.set(true);
                } catch (Throwable e) {
                    inited.set(false);
                    throw new CommunicatorConfigException(e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    String getId() {
        return this.id;
    }

    protected ServantProxyFactory getServantProxyFactory() {
        return servantProxyFactory;
    }

    protected ObjectProxyFactory getObjectProxyFactory() {
        return objectProxyFactory;
    }

    public CommunicatorConfig getCommunicatorConfig() {
        return communicatorConfig;
    }

    protected ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    protected QueryHelper getQueryHelper() {
        return queryHelper;
    }

    public StatHelper getStatHelper() {
        return statHelper;
    }

    public List<EndpointF> getEndpoint4All(String objectName) {
        return getQueryHelper().findObjectById(objectName);
    }
}