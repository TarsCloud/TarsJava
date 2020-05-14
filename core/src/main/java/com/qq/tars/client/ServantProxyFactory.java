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

import com.qq.tars.rpc.common.LoadBalance;
import com.qq.tars.rpc.common.ProtocolInvoker;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class ServantProxyFactory {

    private final ReentrantLock lock = new ReentrantLock();
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

    private final Communicator communicator;

    public ServantProxyFactory(Communicator communicator) {
        this.communicator = communicator;
    }

    public <T> Object getServantProxy(Class<T> clazz, String objName, String setDivision, ServantProxyConfig servantProxyConfig,
                                      LoadBalance loadBalance, ProtocolInvoker<T> protocolInvoker) {
        String key = setDivision != null ? clazz.getSimpleName() + objName + setDivision : clazz.getSimpleName() + objName;
        Object proxy = cache.get(key);
        if (proxy == null) {
            lock.lock();
            try {
                /**
                 * forbid to computeIfAbsent because of deadlock bug in jdk8*/
                proxy = cache.get(key);
                if (proxy == null) {
                    ObjectProxy<T> objectProxy = communicator.getObjectProxyFactory().getObjectProxy(
                            clazz, objName, setDivision, servantProxyConfig, loadBalance, protocolInvoker);
                    cache.put(key, createProxy(clazz, objectProxy));
                    proxy = cache.get(key);
                }
            } finally {
                lock.unlock();
            }
        }
        return proxy;
    }

    private <T> Object createProxy(Class<T> clazz, ObjectProxy<T> objectProxy) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz, ServantProxy.class}, objectProxy);
    }

    public Iterator<Object> getProxyIterator() {
        return cache.values().iterator();
    }
}
