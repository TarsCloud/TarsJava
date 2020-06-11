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
package com.tencent.logback.logserver;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.spi.AppenderAttachable;

public abstract class TarsLoggerServerAppenderConfig<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E> {
    protected Encoder<E> encoder = null;
    protected Integer partition = null;
    protected boolean appendTimestamp = true;
    protected String logserverObjname = null;
    protected String appName = null;
    protected String serviceName = null;

    public String getLogserverObjname() {
        return logserverObjname;
    }

    public void setLogserverObjname(String logserverObjname) {
        this.logserverObjname = logserverObjname;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    protected boolean checkPrerequisites() {
        boolean errorFree = true;
        if (encoder == null) {
            addError("No encoder set for the appender named [\"" + name + "\"].");
            errorFree = false;
        }
        return errorFree;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public boolean isAppendTimestamp() {
        return appendTimestamp;
    }

    public void setAppendTimestamp(boolean appendTimestamp) {
        this.appendTimestamp = appendTimestamp;
    }

}
