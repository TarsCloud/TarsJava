package com.qq.tars.client.rpc.tars;

import com.qq.tars.client.rpc.RPCClient;
import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import com.qq.tars.common.AbstractFilterChain;
import com.qq.tars.common.Filter;
import com.qq.tars.common.FilterKind;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TarsClientFilterChain extends AbstractFilterChain<RPCClient> {

    private Request.InvokeStatus type;

    public TarsClientFilterChain(List<Filter> filters, String servant,
                                 FilterKind kind, RPCClient target, Request.InvokeStatus type) {
        super(filters, servant, kind, target);
        this.type = type;

    }


    @Override
    protected CompletableFuture<Response> doRealInvoke(Request request) throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            return target.send(request);
        } else {
            throw new RuntimeException("[tars] tarsClient Filterchian invoke error!");
        }

    }

    @Override
    protected void doRealInvoke(Request request, Response response) throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            target.send(request);
        } else {
            throw new RuntimeException("[tars] tarsClient Filterchian invoke error!");
        }
    }


    public CompletableFuture<Response> doFilter(Request request) throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            return target.send(request);
        } else {
            throw new RuntimeException("[tars] tarsClient FilterChain invoke error!");
        }
    }

}