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

import com.qq.tars.common.logger.LoggerFactoryManager;
import org.slf4j.Logger;
import org.slf4j.event.Level;

public class LoggerFactory {
    private static final String CLIENT_LOG_NAME = "TARS_CLIENT_LOGGER";
    private static final String OM_LOG_NAME = "OM_LOGGER";
    private static final String TRANSPORTER_LOG_NAME = "TARS_TRANSPORTER_LOGGER";


    public static int resetLogBack() {
        return LoggerFactoryManager.getInstance().getHandler().reloadConfig();
    }

    public static void resetLogLevel(Level level) {
        LoggerFactoryManager.getInstance().getHandler().setLoggerLevel(Logger.ROOT_LOGGER_NAME, level);
    }

    public static Logger getLogger() {
        return org.slf4j.LoggerFactory.getLogger("");
    }


    public static Logger getLogger(String logName) {
        return org.slf4j.LoggerFactory.getLogger(logName);
    }

    public static Logger getOmLogger() {
        return org.slf4j.LoggerFactory.getLogger(OM_LOG_NAME);
    }


    public static Logger getClientLogger() {
        return org.slf4j.LoggerFactory.getLogger(CLIENT_LOG_NAME);
    }

    public static Logger getTransporterLogger() {
        return org.slf4j.LoggerFactory.getLogger(TRANSPORTER_LOG_NAME);
    }
}
