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
package com.tencent.logback.logserver.keying;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAwareBase;

import ch.qos.logback.core.spi.LifeCycle;
import java.nio.ByteBuffer;

public class HostNameKeyingStrategy extends ContextAwareBase implements KeyingStrategy<Object>, LifeCycle {

    private byte[] hostnameHash = null;
    private boolean errorWasShown = false;

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        final String hostname = context.getProperty(CoreConstants.HOSTNAME_KEY);
        if (hostname == null) {
            if (!errorWasShown) {
            addError("Hostname could not be found in context. HostNamePartitioningStrategy will not work.");
                errorWasShown = true;
            }
        } else {
            hostnameHash = ByteBuffer.allocate(4).putInt(hostname.hashCode()).array();
        }
    }

    @Override
    public byte[] createKey(Object e) {
        return hostnameHash;
    }

    @Override
    public void start() { }

    @Override
    public void stop() {
        errorWasShown = false;
    }

    @Override
    public boolean isStarted() { return true; }
}
