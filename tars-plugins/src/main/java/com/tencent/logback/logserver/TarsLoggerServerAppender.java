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

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.CommunicatorFactory;
import com.qq.tars.support.log.prx.LogInfo;
import com.qq.tars.support.log.prx.LogPrx;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TarsLoggerServerAppender<E> extends TarsLoggerServerAppenderConfig<E> {
    private final AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<>();
    private final ConcurrentLinkedQueue<E> queue = new ConcurrentLinkedQueue<>();
    private LoggerServer loggerServer = null;

    public TarsLoggerServerAppender() {
    }


    @Override
    public void doAppend(E e) {
        ensureDeferredAppends();
        super.doAppend(e);
    }

    @Override
    public void start() {
        if (!checkPrerequisites()) return;

        if (partition != null && partition < 0) {
            partition = null;
        }

        this.loggerServer = new LoggerServer();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void addAppender(Appender<E> newAppender) {
        aai.addAppender(newAppender);
    }

    @Override
    public Iterator<Appender<E>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    @Override
    public Appender<E> getAppender(String name) {
        return aai.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<E> appender) {
        return aai.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<E> appender) {
        return aai.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return aai.detachAppender(name);
    }


    private LogInfo getLogInfo() {
        final LogInfo loginfo = new LogInfo();
        loginfo.setAppname(getAppName());
        loginfo.setServername(getServiceName());
        loginfo.setSFilename(this.name);
        loginfo.setSFormat("%Y%m%d%H");
        return loginfo;
    }

    @Override
    protected void append(E e) {
        final byte[] payload = encoder.encode(e);
        final LogPrx logPrx = loggerServer.get();
        if (logPrx != null) {
            final List<String> record = new ArrayList<>(1);
            record.add(new String(payload, StandardCharsets.UTF_8) + "\n");
            try {
                logPrx.promise_loggerbyInfo(getLogInfo(), record);
            } catch (Exception e1) {

                e1.printStackTrace();
            }
        }
    }

    private void deferAppend(E event) {
        queue.add(event);
    }

    // drains queue events to super
    private void ensureDeferredAppends() {
        E event;

        while ((event = queue.poll()) != null) {
            super.doAppend(event);
        }
    }

    /**
     * Lazy initializer for producer, patterned after commons-lang.
     * @see <a href="https://commons.apache.org/proper/commons-lang/javadocs/api-3.4/org/apache/commons/lang3/concurrent/LazyInitializer.html">LazyInitializer</a>
     */
    private class LoggerServer {

        private volatile LogPrx logPrx = null;

        public LogPrx get() {
            LogPrx result = this.logPrx;
            if (result == null) {
                synchronized (this) {
                    result = this.logPrx;
                    if (result == null) {
                        result = this.initialize();
                        this.logPrx = result;
                    }
                }
            }

            return result;
        }

        protected LogPrx initialize() {
            try {
                CommunicatorConfig communicatorConfig = new CommunicatorConfig();
                return CommunicatorFactory.getInstance().getCommunicator(communicatorConfig).stringToProxy(LogPrx.class, logserverObjname);
            } catch (Exception e) {
                e.printStackTrace();
                addError("error creating producer", e);
            }
            return null;
        }
    }

}
