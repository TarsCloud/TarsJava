package com.qq.tars.client.rpc;

import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import io.netty.channel.Channel;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class TicketFeature extends CompletableFuture<Object> {
    public static final int DEFAULT_TICKET_NUMBER = -1;
    private Response response = null;
    private final Request request;
    private final Channel channel;
    private final int id;
    protected long timeout = 1000;
    private static final Map<Integer, TicketFeature> featureMap = new ConcurrentHashMap<>();
    Future<?> timeoutFuture;

    public Future<?> getTimeoutFuture() {
        return timeoutFuture;
    }

    public void setTimeoutFuture(Future<?> timeoutFuture) {
        this.timeoutFuture = timeoutFuture;
    }


    public TicketFeature(Channel channel, Request request, long timeout) {
        this.request = request;
        this.timeout = timeout;
        this.channel = channel;
        this.id = request.getRequestId();
        featureMap.put(request.getRequestId(), this);
    }


    public static TicketFeature createFeature(Channel channel, Request request, int timeout) {
        TicketFeature ticketFeature = new TicketFeature(channel, request, timeout);
        return ticketFeature;
    }

    public static TicketFeature getFeature(int requestId) {
        return featureMap.get(requestId);
    }

    public void received(Response response) {
        if (response == null) {
            throw new RuntimeException("response is null!");
        }

        TarsServantResponse tarsResponse = (TarsServantResponse) response;
        this.complete(tarsResponse);

    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Request getRequest() {
        return request;
    }

    public Channel getChannel() {
        return channel;
    }

    public int getId() {
        return id;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }


    public void cancel() {
        this.cancel(true);
    }
}
