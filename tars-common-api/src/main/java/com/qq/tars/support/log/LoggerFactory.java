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

package com.qq.tars.support.log;

import java.util.logging.Logger;

public final class LoggerFactory {
    private static final String CLIENT_LOG_NAME = "TARS_CLIENT_LOGGER";
    private static final String OM_LOG_NAME = "OM_LOGGER";
    private static final String TRANSPORTER_LOG_NAME = "TARS_TRANSPORTER_LOGGER";

    private LoggerFactory() {
    }

    public static Logger getLogger(String name) {
        return Logger.getLogger(name);
    }

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    public static Logger getOmLogger() {
        return Logger.getLogger(OM_LOG_NAME);
    }

    public static Logger getClientLogger() {
        return Logger.getLogger(CLIENT_LOG_NAME);
    }

    public static Logger getTransporterLogger() {
        return Logger.getLogger(TRANSPORTER_LOG_NAME);
    }
}
