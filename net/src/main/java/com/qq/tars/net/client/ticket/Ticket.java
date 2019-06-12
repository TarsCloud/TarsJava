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

package com.qq.tars.net.client.ticket;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.qq.tars.net.client.Callback;
import com.qq.tars.net.core.Request;
import com.qq.tars.net.core.nio.SelectorManager;

public class Ticket<T> {

    public static final int DEFAULT_TICKET_NUMBER = -1;
    private CountDownLatch latch = new CountDownLatch(1);

    private T response = null;
    private Request request = null;
    private volatile boolean expired = false;
    protected long timeout = 1000;
    public long startTime = System.currentTimeMillis();
    private Callback<T> callback = null;
    private int ticketNumber = -1;
    private static TicketListener ticketListener = null;

    private SelectorManager selectorManager = null;

    Future<?> timeoutFuture;

    AtomicBoolean hasRun = new AtomicBoolean(false);

    public Future<?> getTimeoutFuture() {
        return timeoutFuture;
    }

    public void setTimeoutFuture(Future<?> timeoutFuture) {
        this.timeoutFuture = timeoutFuture;
    }

    public SelectorManager getSelectorManager() {
        return selectorManager;
    }

    public void setSelectorManager(SelectorManager selectorManager) {
        this.selectorManager = selectorManager;
    }


    public Ticket(Request request, long timeout) {
        this.request = request;
        this.ticketNumber = request.getTicketNumber();
        this.timeout = timeout;
    }

    public Ticket(Request request, long timeout,SelectorManager selectorManager) {
        this.request = request;
        this.ticketNumber = request.getTicketNumber();
        this.timeout = timeout;
        this.selectorManager = selectorManager;
    }

    public Request request() {
        return this.request;
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        boolean status = this.latch.await(timeout, unit);
        checkExpired();
        return status;
    }

    public void await() throws InterruptedException {
        this.latch.await();
        checkExpired();
    }

    public void expired() {
        if(hasRun.compareAndSet(false, true)) {
            this.expired = true;
            if (callback != null) {
                //超时回调，用业务线程池去处理,防止业务耗时，影响已有的超时任务线程
                if(getTimeoutFuture()!= null && (getTimeoutFuture().isDone() || getTimeoutFuture().isCancelled())) {   //超时任务已经执行或者已经被取消，已经通知了业务方超时信息(防止超时消息二次通知)
                    System.out.println("task has run or canceled.");
                } else {
                    selectorManager.getThreadPool().execute(() -> callback.onExpired());
                }
            }
            this.countDown();
            if (ticketListener != null) ticketListener.onResponseExpired(this);
        } else {
            System.out.println("expired has run.");
        }
    }

    public void countDown() {
        this.latch.countDown();
    }

    public boolean isDone() {
        return this.latch.getCount() == 0;
    }

    public void notifyResponse(T response) {
        this.response = response;
        if (this.callback != null) this.callback.onCompleted(response);
        if (ticketListener != null) ticketListener.onResponseReceived(this);
    }

    public T response() {
        return this.response;
    }

    public Callback<T> getCallback() {
        return callback;
    }

    public void setCallback(Callback<T> callback) {
        this.callback = callback;
    }

    public int getTicketNumber() {
        return this.ticketNumber;
    }

    protected void checkExpired() {
        if (this.expired) throw new RuntimeException("", new IOException("The operation has timed out."));
    }

    public static void setTicketListener(TicketListener listener) {
        ticketListener = listener;
    }
}
