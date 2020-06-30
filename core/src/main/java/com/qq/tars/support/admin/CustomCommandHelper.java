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

package com.qq.tars.support.admin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CustomCommandHelper {

    private static final CustomCommandHelper Instance = new CustomCommandHelper();

    private Map<String, CommandHandler> customHandlerMap = new ConcurrentHashMap<String, CommandHandler>();

    private CustomCommandHelper() {
    }

    public static CustomCommandHelper getInstance() {
        return Instance;
    }

    public boolean registerCustomHandler(String cmdName, CommandHandler handler) {
        if (com.qq.tars.common.util.StringUtils.isEmpty(cmdName) || handler == null) {
            return false;
        }
        customHandlerMap.put(cmdName, handler);
        return true;
    }

    public CommandHandler getCommandHandler(String cmdName) {
        return customHandlerMap.get(cmdName);
    }
}
