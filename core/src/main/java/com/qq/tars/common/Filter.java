package com.qq.tars.common;


import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;

import java.util.concurrent.CompletableFuture;

public interface Filter {

    void init();

    void doFilter(Request request, Response response, FilterChain chain) throws Throwable;

    CompletableFuture<Response> doFilter(Request request, FilterChain chain) throws Throwable;

    void destroy();

}
