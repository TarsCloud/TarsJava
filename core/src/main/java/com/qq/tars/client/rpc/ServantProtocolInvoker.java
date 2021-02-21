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

package com.qq.tars.client.rpc;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.util.ParseTools;
import com.qq.tars.common.support.ScheduledExecutorManager;
import com.qq.tars.common.util.Constants;
import com.qq.tars.rpc.common.Invoker;
import com.qq.tars.rpc.common.ProtocolInvoker;
import com.qq.tars.rpc.common.Url;
import com.qq.tars.rpc.common.util.concurrent.ConcurrentHashSet;
import com.qq.tars.rpc.exc.ClientException;
import com.qq.tars.support.log.LoggerFactory;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class ServantProtocolInvoker<T> implements ProtocolInvoker<T> {
    private static final Logger logger = LoggerFactory.getClientLogger();
    protected final Class<T> api;
    protected final ServantProxyConfig servantProxyConfig;
    protected volatile ConcurrentHashSet<Invoker<T>> allInvoker = new ConcurrentHashSet<>();


    public ServantProtocolInvoker(Class<T> api, ServantProxyConfig config) {
        this.api = api;
        this.servantProxyConfig = config;
        this.allInvoker = this.initInvoker();
    }

    public abstract Invoker<T> create(Class<T> api, Url url) throws Exception;

    public Collection<Invoker<T>> getInvokers() {
        return Collections.unmodifiableCollection(allInvoker);
    }

    public void destroy() {
        destroy(allInvoker);
    }

    public void refresh() {
        logger.info("try to refresh " + servantProxyConfig.getSimpleObjectName());
        Set<Url> currentUrls = new HashSet<>(ParseTools.parse(servantProxyConfig));
        Map<Url, Invoker<T>> prevUrlInvokerMap = new HashMap<>(allInvoker.size());
        List<Invoker<T>> brokenInvokers = new ArrayList<>();
        for (Invoker<T> invoker : allInvoker) {
            Url url = invoker.getUrl();
            prevUrlInvokerMap.put(url, invoker);
            if (!currentUrls.contains(url)) {
                brokenInvokers.add(invoker);
            }
        }
        List<Url> newUrls = new ArrayList<>(currentUrls.size());
        for (Url url : currentUrls) {
            if (!prevUrlInvokerMap.containsKey(url)) {
                newUrls.add(url);
            }
        }

        addInvokers(newUrls);
        // only destroy broken invokers
        ScheduledExecutorManager.getInstance().schedule(() -> destroy(brokenInvokers), Math.max(servantProxyConfig.getAsyncTimeout(), servantProxyConfig.getSyncTimeout()), TimeUnit.MILLISECONDS);
    }

    protected RPCClient[] getClients(Url url) {
        int connections = url.getParameter(Constants.TARS_CLIENT_CONNECTIONS, Constants.DEFAULT_CONNECTION);
        RPCClient[] clients = new RPCClient[connections];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = initClient(url);
        }
        return clients;
    }

    protected RPCClient initClient(Url url) {
        try {
            return TransporterAbstractFactory.getInstance().getTransporterFactory().connect(url, servantProxyConfig);
        } catch (Throwable e) {
            throw new ClientException(servantProxyConfig.getSimpleObjectName(), "Fail to create client|" + url.toIdentityString() + "|" + e.getLocalizedMessage(), e);
        }
    }

    protected ConcurrentHashSet<Invoker<T>> initInvoker() {
        try {
            logger.info("try to init invoker|conf={}" + servantProxyConfig.toString());
            final List<Url> list = ParseTools.parse(servantProxyConfig);
            return createInvokers(list);
        } catch (Throwable t) {
            logger.error("error occurred on init invoker|" + servantProxyConfig.getObjectName(), t);
        }
        return new ConcurrentHashSet<>();
    }

    private void addInvokers(Collection<Url> urls) {
        logger.info("try to add invokers|url={}", urls.stream().map(Url::toIdentityString).collect(Collectors.toList()));
        ConcurrentHashSet<Invoker<T>> invokers = createInvokers(urls);
        if (!invokers.isEmpty()) {
            allInvoker.addAll(invokers);
        }
    }

    private ConcurrentHashSet<Invoker<T>> createInvokers(Collection<Url> list) {
        final ConcurrentHashSet<Invoker<T>> invokers = new ConcurrentHashSet<>();
        for (Url url : list) {
            try {
                boolean active = url.getParameter(Constants.TARS_CLIENT_ACTIVE, false);
                if (active) {
                    logger.info("try to init invoker|active={} |{}", active, url.toIdentityString());
                    invokers.add(create(api, url));
                } else {
                    logger.info("inactive invoker can't to init|active={}|{}", active, url.toIdentityString());
                }
            } catch (Throwable e) {
                logger.error("error occurred on init invoker|" + url.toIdentityString(), e);
            }
        }
        return invokers;
    }

    private void destroy(Collection<Invoker<T>> invokers) {
        for (Invoker<?> invoker : invokers) {
            if (invoker != null) {
                logger.info("destroy reference|" + invoker);
                try {
                    allInvoker.remove(invoker);
                    invoker.destroy();
                } catch (Throwable t) {
                    logger.error("error occurred on destroy invoker|" + invoker, t);
                }
            }
        }
    }
}
