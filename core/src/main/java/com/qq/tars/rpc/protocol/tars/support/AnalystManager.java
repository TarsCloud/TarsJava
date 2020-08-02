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

package com.qq.tars.rpc.protocol.tars.support;

import com.qq.tars.protocol.tars.support.TarsMethodInfo;
import com.qq.tars.protocol.util.TarsHelper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class AnalystManager {

    private ConcurrentHashMap<String, Map<Method, TarsMethodInfo>> cache = new ConcurrentHashMap<String, Map<Method, TarsMethodInfo>>();
    private ConcurrentHashMap<String, Map<String, TarsMethodInfo>> context_cache = new ConcurrentHashMap<String, Map<String, TarsMethodInfo>>();

    public static AnalystManager getInstance() {
        return instance;
    }
    private final static AnalystManager instance = new AnalystManager();

    public Map<Method, TarsMethodInfo> getMethodMap(Class<?> api) {
        return cache.get(api.getName());
    }

    public void registry(Class<?> api, String objName) {
        cache.putIfAbsent(api.getName(), TarsHelper.getMethodInfo(api, objName));
    }

    public Map<String, TarsMethodInfo> getMethodMapByName(String objName) {
        return context_cache.get(objName);
    }

    public void registry(Class<?> api, Object servant, String objName) {
        Map<Method, TarsMethodInfo> methodsMap = TarsHelper.getMethodInfo(api, servant, objName);
        if (methodsMap != null && !methodsMap.isEmpty()) {
            Map<String, TarsMethodInfo> methodMap = new HashMap<String, TarsMethodInfo>();
            for (Entry<Method, TarsMethodInfo> entry : methodsMap.entrySet()) {
                methodMap.put(entry.getKey().getName(), entry.getValue());
            }
            context_cache.putIfAbsent(objName, methodMap);
        }
    }
}
