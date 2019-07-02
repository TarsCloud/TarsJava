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

package com.qq.tars.server.common;

import java.io.PrintStream;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.support.log.Logger;
import com.qq.tars.support.log.LoggingOutputStream;
import com.qq.tars.support.log.Logger.LogType;

public final class ServerLogger
{

    private static final String STDOUT_log_NAME = "stdout.log";
    private static final String STDERR_LOG_NAME = "stderr.log";
    private static final String SERVER_LOG_NAME = "tarsserver.log";

    public static void init()
    {
        boolean flag = ConfigurationManager.getInstance().getServerConfig().isLogDebug();
        if (flag == false)
        {
            int logType = ConfigurationManager.getInstance().getServerConfig().getLogType();
            LogType type = LogType.LOCAL;

            if (LogType.REMOTE.getValue() == logType)
            {
                type = LogType.REMOTE;
            } else if (LogType.ALL.getValue() == logType)
            {
                type = LogType.ALL;
            }

            System.setOut(new PrintStream(new LoggingOutputStream(Logger.getLogger(STDOUT_log_NAME, type)), true));
            System.setErr(new PrintStream(new LoggingOutputStream(Logger.getLogger(STDERR_LOG_NAME, type)), true));
        }
    }

    public static void initNamiCoreLog(String logPath, String logLevel)
    {
    }

    public static Logger stdout()
    {
        return Logger.getLogger(STDOUT_log_NAME);
    }

    public static Logger stderr()
    {
        return Logger.getLogger(STDERR_LOG_NAME);
    }

    public static Logger flow()
    {
        return Logger.getLogger(SERVER_LOG_NAME);
    }
}
