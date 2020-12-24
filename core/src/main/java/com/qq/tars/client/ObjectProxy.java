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

import com.qq.tars.client.support.ServantCacheManager;
import com.qq.tars.client.util.ParseTools;
import com.qq.tars.common.support.ScheduledExecutorManager;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.register.RegisterManager;
import com.qq.tars.rpc.common.InvokeContext;
import com.qq.tars.rpc.common.Invoker;
import com.qq.tars.rpc.common.LoadBalance;
import com.qq.tars.rpc.common.ProtocolInvoker;
import com.qq.tars.rpc.common.Url;
import com.qq.tars.rpc.common.exc.NoInvokerException;
import com.qq.tars.rpc.exc.ClientException;
import com.qq.tars.rpc.exc.NoConnectionException;
import com.qq.tars.support.log.LoggerFactory;
import com.qq.tars.support.stat.InvokeStatHelper;
import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ObjectProxy<T> implements ServantProxy, InvocationHandler {
    private static final Logger logger = LoggerFactory.getClientLogger();

    private final Class<T> api;
    // private final String objName;
    private final Communicator communicator;

    private final ServantCacheManager servantCacheManager = ServantCacheManager.getInstance();

    private volatile ServantProxyConfig servantProxyConfig;

    private LoadBalance loadBalancer;
    private ProtocolInvoker<T> protocolInvoker;
    private ScheduledFuture<?> statReportFuture;
    private ScheduledFuture<?> queryRefreshFuture;

    private final Object refreshLock = new Object();

    private final Random random = new Random(System.currentTimeMillis() / 1000);

    public ObjectProxy(Class<T> api, ServantProxyConfig servantProxyConfig, LoadBalance loadBalance,
            ProtocolInvoker<T> protocolInvoker, Communicator communicator) {
        this.api = api;
        // this.objName = objName;
        this.communicator = communicator;
        this.servantProxyConfig = servantProxyConfig;
        this.loadBalancer = loadBalance;
        this.protocolInvoker = protocolInvoker;
        this.initialize();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        InvokeContext context = protocolInvoker.createContext(proxy, method, args);
        try {
            if ("toString".equals(methodName) && parameterTypes.length == 0) {
                return this.toString();
            } else if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
                return this.hashCode();
            } else if ("equals".equals(methodName) && parameterTypes.length == 1) {
                return this.equals(args[0]);
            } else if ("getObjectName".equals(methodName) && parameterTypes.length == 0) {
                return this.getObjectName();
            } else if ("getApi".equals(methodName) && parameterTypes.length == 0) {
                return this.getApi();
            } else if ("getConfig".equals(methodName) && parameterTypes.length == 0) {
                return this.getConfig();
            } else if ("destroy".equals(methodName) && parameterTypes.length == 0) {
                this.destroy();
                return null;
            } else if ("refresh".equals(methodName) && parameterTypes.length == 0) {
                this.refresh();
                return null;
            }

            Invoker invoker = loadBalancer.select(context);
            return invoker.invoke(context);
        } catch (Throwable e) {
            e.printStackTrace();
            if (logger.isDebugEnabled()) {
                logger.debug(servantProxyConfig.getSimpleObjectName() + " error occurred on invoke|"
                        + e.getLocalizedMessage(), e);
            }
            if (e instanceof NoInvokerException) {
                throw new NoConnectionException(servantProxyConfig.getSimpleObjectName(), e.getLocalizedMessage(), e);
            }
            throw new ClientException(servantProxyConfig.getSimpleObjectName(), e.getLocalizedMessage(), e);
        }
    }

    public Url selectUrl() {
        return loadBalancer.select(null).getUrl();
    }

    public Class<T> getApi() {
        return api;
    }

    public String getObjectName() {
        return servantProxyConfig.getSimpleObjectName();
    }

    public void refresh() {
        synchronized (refreshLock) {
            registryStatReporter();
            registryServantNodeRefresher();
            protocolInvoker.refresh();
            loadBalancer.refresh(protocolInvoker.getInvokers());
        }
    }

    public void destroy() {
        if (statReportFuture != null)
            statReportFuture.cancel(false);
        if (queryRefreshFuture != null)
            queryRefreshFuture.cancel(false);
        protocolInvoker.destroy();
    }

    public ServantProxyConfig getConfig() {
        return servantProxyConfig;
    }

    private void initialize() {
        loadBalancer.refresh(protocolInvoker.getInvokers());

        if (StringUtils.isNotEmpty(this.servantProxyConfig.getLocator())
                && !StringUtils.isEmpty(this.servantProxyConfig.getStat())) {
            this.registryStatReporter();
        }
        if (!servantProxyConfig.isDirectConnection()) {
            this.registryServantNodeRefresher();
        }
    }

    private void registryStatReporter() {
        if (this.statReportFuture != null && !this.statReportFuture.isCancelled()) {
            this.statReportFuture.cancel(false);
        }
        if (!StringUtils.isEmpty(communicator.getCommunicatorConfig().getStat())) {
            int interval = servantProxyConfig.getReportInterval();
            int initialDelay = interval + (random.nextInt(30) * 1000);
            this.statReportFuture = ScheduledExecutorManager.getInstance()
                    .scheduleAtFixedRate(new ServantStatReporter(), initialDelay, interval, TimeUnit.MILLISECONDS);
        }
    }

    private void registryServantNodeRefresher() {
        if (this.queryRefreshFuture != null && !this.queryRefreshFuture.isCancelled()) {
            this.queryRefreshFuture.cancel(false);
        }
        if (!servantProxyConfig.isDirectConnection()) {
            int interval = servantProxyConfig.getRefreshInterval();
            int initialDelay = interval + (random.nextInt(30) * 1000);
            this.queryRefreshFuture = ScheduledExecutorManager.getInstance()
                    .scheduleAtFixedRate(new ServantNodeRefresher(), initialDelay, interval, TimeUnit.MILLISECONDS);
        }
    }

    private class ServantNodeRefresher implements Runnable {

        public void run() {
            long begin = System.currentTimeMillis();
            try {
                String nodes;
                if (RegisterManager.getInstance().getHandler() != null) {
                    nodes = ParseTools.parse(
                            RegisterManager.getInstance().getHandler().query(servantProxyConfig.getSimpleObjectName()),
                            servantProxyConfig.getSimpleObjectName());
                } else {
                    nodes = communicator.getQueryHelper().getServerNodes(servantProxyConfig);
                }
                if (nodes != null && !nodes.equals(servantProxyConfig.getObjectName())) {
                    servantCacheManager.save(communicator.getId(), servantProxyConfig.getSimpleObjectName(), nodes,
                            communicator.getCommunicatorConfig().getDataPath());
                    servantProxyConfig.setObjectName(nodes);
                    refresh();
                }
                logger.debug("{} sync server|{}", servantProxyConfig.getSimpleObjectName(), nodes);
            } catch (Throwable e) {
                logger.error(servantProxyConfig.getSimpleObjectName() + " error sync server", e);
            } finally {
                logger.info("ServantNodeRefresher run({}), use: {}", servantProxyConfig.getSimpleObjectName(),
                        (System.currentTimeMillis() - begin));
            }
        }
    }

    private class ServantStatReporter implements Runnable {

        public void run() {
            long begin = System.currentTimeMillis();
            try {
                communicator.getStatHelper().report(
                        InvokeStatHelper.getInstance().getProxyStat(servantProxyConfig.getSimpleObjectName()), true);
            } catch (Exception e) {
                logger.error("report stat worker error|" + servantProxyConfig.getSimpleObjectName(), e);
            } finally {
                logger.info("ServantStatReproter run(), use: " + (System.currentTimeMillis() - begin));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("hello world");
    }
}
