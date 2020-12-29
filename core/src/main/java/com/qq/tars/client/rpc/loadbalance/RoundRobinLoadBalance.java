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


package com.qq.tars.client.rpc.loadbalance;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.cluster.ServantInvokerAliveChecker;
import com.qq.tars.client.cluster.ServantInvokerAliveStat;
import com.qq.tars.client.rpc.InvokerComparator;
import com.qq.tars.common.util.CollectionUtils;
import com.qq.tars.rpc.common.InvokeContext;
import com.qq.tars.rpc.common.Invoker;
import com.qq.tars.rpc.common.LoadBalance;
import com.qq.tars.rpc.common.exc.NoInvokerException;
import com.qq.tars.support.log.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Load balancer strategy
 */
public class RoundRobinLoadBalance<T> implements LoadBalance<T> {
    private static final Logger logger = LoggerFactory.getClientLogger();


    private final AtomicInteger sequence = new AtomicInteger();
    private final AtomicInteger staticWeightSequence = new AtomicInteger();
    private final ServantProxyConfig config;
    private final InvokerComparator comparator = new InvokerComparator();

    private volatile List<Invoker<T>> sortedInvokersCache = null;
    private volatile List<Invoker<T>> staticWeightInvokersCache = null;

    public RoundRobinLoadBalance(ServantProxyConfig config) {
        this.config = config;
    }

    @Override
    public Invoker<T> select(InvokeContext invocation) throws NoInvokerException {
        List<Invoker<T>> staticWeightInvokers = staticWeightInvokersCache;

        //polling  use weights
        if (staticWeightInvokers != null && !staticWeightInvokers.isEmpty()) {
            Invoker<T> invoker = staticWeightInvokers.get((staticWeightSequence.getAndIncrement() & Integer.MAX_VALUE) % staticWeightInvokers.size());
            if (invoker.isAvailable()) return invoker;

            ServantInvokerAliveStat stat = ServantInvokerAliveChecker.get(invoker.getUrl());
            if (stat.isAlive() || (stat.getLastRetryTime() + (config.getTryTimeInterval() * 1000)) < System.currentTimeMillis()) {
                //Try to recall after blocking
                logger.info("try to use inactive invoker|" + invoker.getUrl().toIdentityString());
                stat.setLastRetryTime(System.currentTimeMillis());
                return invoker;
            }
        }

        //polling without weights
        List<Invoker<T>> sortedInvokers = sortedInvokersCache;
        if (CollectionUtils.isEmpty(sortedInvokers)) {
            throw new NoInvokerException("no such active connection invoker");
        }

        List<Invoker<T>> list = new ArrayList<Invoker<T>>();
        for (Invoker<T> invoker : sortedInvokers) {
            if (!invoker.isAvailable()) {
                /**
                 * Try to recall after blocking
                 */
                ServantInvokerAliveStat stat = ServantInvokerAliveChecker.get(invoker.getUrl());
                if (stat.isAlive() || (stat.getLastRetryTime() + (config.getTryTimeInterval() * 1000)) < System.currentTimeMillis()) {
                    list.add(invoker);
                }
            } else {
                list.add(invoker);
            }
        }
        //TODO When all is not available. Whether to randomly extract one
        if (list.isEmpty()) {
            throw new NoInvokerException(config.getSimpleObjectName() + " try to select active invoker, size=" + sortedInvokers.size() + ", no such active connection invoker");
        }

        Invoker<T> invoker = list.get((sequence.getAndIncrement() & Integer.MAX_VALUE) % list.size());

        if (!invoker.isAvailable()) {
            //Try to recall after blocking
            logger.info("try to use inactive invoker|" + invoker.getUrl().toIdentityString());
            ServantInvokerAliveChecker.get(invoker.getUrl()).setLastRetryTime(System.currentTimeMillis());
        }
        return invoker;
    }

    @Override
    public void refresh(Collection<Invoker<T>> invokers) {
        logger.info("{} try to refresh RoundRobinLoadBalance's invoker cache, size= {} ", config.getSimpleObjectName(), CollectionUtils.isEmpty(invokers) ? 0 : invokers.size());
        if (CollectionUtils.isEmpty(invokers)) {
            sortedInvokersCache = null;
            staticWeightInvokersCache = null;
            return;
        }
        final List<Invoker<T>> sortedInvokersTmp = new ArrayList<Invoker<T>>(invokers);
        Collections.sort(sortedInvokersTmp, comparator);
        sortedInvokersCache = sortedInvokersTmp;
        staticWeightInvokersCache = LoadBalanceHelper.buildStaticWeightList(sortedInvokersTmp, config);
        logger.info("{} refresh RoundRobinLoadBalance's invoker cache done, staticWeightInvokersCache size= {}, sortedInvokersCache size={}",
                config.getSimpleObjectName(),
                (staticWeightInvokersCache == null || staticWeightInvokersCache.isEmpty() ? 0 : staticWeightInvokersCache.size()),
                (sortedInvokersCache == null || sortedInvokersCache.isEmpty() ? 0 : sortedInvokersCache.size()))
        ;
    }

}
