package com.qq.tars.server.core;

import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import com.qq.tars.common.AbstractFilterChain;
import com.qq.tars.common.Filter;
import com.qq.tars.common.FilterKind;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TarsServerFilterChain extends AbstractFilterChain<ServantHomeSkeleton> {

    public TarsServerFilterChain(List<Filter> filters, String servant,
                                 FilterKind kind, ServantHomeSkeleton target) {
        super(filters, servant, kind, target);
    }

    @Override
    protected void doRealInvoke(Request request, Response response)
            throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            TarsServantRequest tarsServantRequest = (TarsServantRequest) request;
            Object value = target.invoke(tarsServantRequest.getMethodInfo().getMethod(), tarsServantRequest.getMethodParameters());
            TarsServantResponse tarsServantResponse = (TarsServantResponse) response;
            tarsServantResponse.setResult(value);
        }
    }

    @Override
    protected CompletableFuture<Response> doRealInvoke(Request request) throws Throwable {
        return null;
    }

    @Override
    public CompletableFuture<Response> doFilter(Request request) throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            TarsServantRequest tarsServantRequest = (TarsServantRequest) request;
            Object value = target.invoke(tarsServantRequest.getMethodInfo().getMethod(), tarsServantRequest.getMethodParameters());
            TarsServantResponse tarsServantResponse = new TarsServantResponse(request.getRequestId());
            tarsServantResponse.setResult(value);
            return CompletableFuture.completedFuture(tarsServantResponse);
        } else {
            throw new RuntimeException("[Tars] invoke error!");
        }
    }
}
