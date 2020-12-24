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

package com.qq.tars.client.rpc;

import com.qq.tars.net.client.Callback;
import com.qq.tars.net.client.ticket.Ticket;
import com.qq.tars.net.client.ticket.TicketManager;
import com.qq.tars.net.core.Request.InvokeStatus;
import com.qq.tars.net.core.Session;
import com.qq.tars.net.core.Session.SessionStatus;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.rpc.exc.TimeoutException;
import com.qq.tars.rpc.protocol.ServantRequest;
import com.qq.tars.rpc.protocol.ServantResponse;
import com.qq.tars.support.log.LoggerFactory;
import org.slf4j.Logger;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ServantClient implements RPCClient {
    private static final Logger logger = LoggerFactory.getClientLogger();

    private Session session = null;
    private String host = null;
    private int port = -1;
    private long asyncTimeout = 1000;
    private long syncTimeout = 1000;
    private long connectTimeout = 200;
    private boolean udpMode = false;
    private int tc = INVALID_TRAFFIC_CLASS_VALUE;
    private int bufferSize = 1024 * 4;
    private boolean tcpNoDelay = false;
    private static final int INVALID_TRAFFIC_CLASS_VALUE = -1;

    public ServantClient(String ip, int port, boolean udpMode) {
        this.host = ip;
        this.port = port;
        this.udpMode = udpMode;
    }

    public synchronized void reConnect() throws IOException {

    }

    public void ensureConnected() throws IOException {

    }

    public <T extends ServantResponse> T invokeWithSync(ServantRequest request) throws IOException {
        Ticket<T> ticket = null;
        T response = null;
        try {
            ensureConnected();
            request.setInvokeStatus(InvokeStatus.SYNC_CALL);
            ticket = TicketManager.createTicket(request, session, this.syncTimeout);

            Session current = session;
            current.write(request);
            if (!ticket.await(this.syncTimeout, TimeUnit.MILLISECONDS)) {
                if (current != null && current.getStatus() != SessionStatus.CLIENT_CONNECTED) {
                    throw new IOException("Connection reset by peer|" + this.getAddress());
                } else {
                    throw new TimeoutException("the operation has timeout, " + this.syncTimeout + "ms|" + this.getAddress());
                }
            }
            response = ticket.response();
            if (response == null) {
                throw new IOException("the operation is failed.");
            }
            return response;
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
        } finally {
            if (ticket != null) {
                TicketManager.removeTicket(ticket.getTicketNumber());
            }
        }
    }

    public <T extends ServantResponse> void invokeWithAsync(ServantRequest request, Callback<T> callback) throws IOException {
        Ticket<T> ticket = null;

        try {
            ensureConnected();
            request.setInvokeStatus(InvokeStatus.ASYNC_CALL);
            ticket = TicketManager.createTicket(request, session, this.asyncTimeout);

            Session current = session;
            current.write(request);
        } catch (Exception ex) {
            if (ticket != null) {
                TicketManager.removeTicket(ticket.getTicketNumber());
            }
            throw new IOException("error occurred on invoker with async", ex);
        }
    }

    public <T extends ServantResponse> void invokeWithFuture(ServantRequest request, Callback<T> callback) throws IOException {
        Ticket<T> ticket = null;
        try {
            ensureConnected();
            request.setInvokeStatus(InvokeStatus.FUTURE_CALL);
            ticket = TicketManager.createTicket(request, session, this.asyncTimeout, callback);

            Session current = session;
            current.write(request);
        } catch (Exception ex) {
            if (ticket != null) {
                TicketManager.removeTicket(ticket.getTicketNumber());
            }
            throw new IOException("error occurred on invoker with future", ex);
        }
    }

    private synchronized void shutdown() throws IOException {
        if (this.session != null) {
            this.session.asyncClose();
        }
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Session getIoSession() {
        return this.session;
    }

    public int getTrafficClass() {
        return tc;
    }

    public void setTrafficClass(int tc) {

    }

    public void setTcpNoDelay(boolean on) {

    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getSyncTimeout() {
        return syncTimeout;
    }

    public void setSyncTimeout(long syncTimeout) {
        this.syncTimeout = syncTimeout;
    }

    public void setAsyncTimeout(long asyncTimeout) {
        this.asyncTimeout = asyncTimeout;
    }

    public void close() throws IOException {
        this.shutdown();
    }

    private boolean isNotConnected() {
        return session == null || session.getStatus() == SessionStatus.CLOSED || session.getStatus() == SessionStatus.NOT_CONNECTED;
    }

    public String getAddress() {
        return host + ":" + port;
    }

    public String toString() {
        return "ServantClient [client=" + getAddress() + "]";
    }



    public static void main(String[] args){


        TarsOutputStream tarsOutputStream  = new TarsOutputStream();
    }
}
