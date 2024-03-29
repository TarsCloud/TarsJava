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

package com.qq.tars.server.ha;

import com.qq.tars.net.core.Session.SessionStatus;
import com.qq.tars.net.core.SessionEvent;
import com.qq.tars.net.core.SessionListener;
import com.qq.tars.support.log.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionSessionListener implements SessionListener {

    private final AtomicInteger connStat = new AtomicInteger(0);

    private final int MaxConnCount;

    public ConnectionSessionListener(int connCount) {
        MaxConnCount = connCount;
        LoggerFactory.getOmLogger().debug("MaxConnCount={}", MaxConnCount);
    }

    @Override
    public synchronized void onSessionCreated(SessionEvent se) {
        if (connStat.get() >= MaxConnCount) {
            try {
                LoggerFactory.getOmLogger().info("reached the max connection threshold, close the session.");
                se.getSession().close();
            } catch (IOException e) {
            }
            return;
        }
        LoggerFactory.getOmLogger().debug("onSessionCreated:{}", connStat.incrementAndGet());
    }

    @Override
    public synchronized void onSessionDestroyed(SessionEvent se) {
        if ((se.getSession() == null || se.getSession().getStatus() == SessionStatus.CLOSED) && connStat.get() > 0) {
            LoggerFactory.getOmLogger().info("onSessionDestroyed: " + connStat.decrementAndGet());
        }
    }
}
