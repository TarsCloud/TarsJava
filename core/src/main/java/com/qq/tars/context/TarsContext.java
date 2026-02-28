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
package com.qq.tars.context;

import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;

import java.util.HashMap;
import java.util.Map;

public final class TarsContext {

    public static final class Key<T> {
        private final String name;

        private Key(String name) {
            this.name = name;
        }

        public static <T> Key<T> named(String name) {
            return new Key<>(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Dyeing keys
    public static final Key<Boolean> DYEING = Key.named("dyeing");
    public static final Key<String> DYEING_KEY = Key.named("dyeingKey");
    public static final Key<String> DYEING_FILENAME = Key.named("dyeFileName");

    // Request processing keys
    public static final Key<Request> REQUEST = Key.named("request");
    public static final Key<Response> RESPONSE = Key.named("response");
    public static final Key<String> SERVANT_NAME = Key.named("servantName");

    private static final ThreadLocal<TarsContext> CURRENT = ThreadLocal.withInitial(TarsContext::new);

    private final Map<Key<?>, Object> values = new HashMap<>();

    public static TarsContext current() {
        return CURRENT.get();
    }

    public static void release() {
        CURRENT.remove();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T) values.get(key);
    }

    public <T> void set(Key<T> key, T value) {
        values.put(key, value);
    }
}
