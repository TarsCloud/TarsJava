package com.qq.tars.common;


import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;

import java.util.concurrent.CompletableFuture;

public interface FilterChain {

    void doFilter(Request request, Response response) throws Throwable;

    CompletableFuture<Response> doFilter(Request request) throws Throwable;
}
