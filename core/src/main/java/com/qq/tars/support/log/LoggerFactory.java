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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class LoggerFactory {
    private static final LoggerContext logContext;
    private static final String CLIENT_LOG_NAME = "TARS_CLIENT_LOGGER";
    private static final String OM_LOG_NAME = "OM_LOGGER";


    static {
        logContext = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
    }


    public static int resetLogBack() {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(logContext);
        logContext.reset();
        try {
            configurator.doConfigure("logback.xml");
            return 0;
        } catch (JoranException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void resetLogLevel(Level level) {
        Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    public static Logger getLogger() {
        return logContext.getLogger("");
    }


    public static Logger getLogger(String logName) {
        return logContext.getLogger(logName);
    }

    public static Logger getOmLogger() {
        return logContext.getLogger(OM_LOG_NAME);
    }


    public static Logger getClientLogger() {
        return LoggerFactory.getLogger(CLIENT_LOG_NAME);
    }

}
