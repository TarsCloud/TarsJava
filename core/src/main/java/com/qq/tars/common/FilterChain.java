package com.qq.tars.common;


import com.qq.tars.client.rpc.Request;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;

import java.util.concurrent.CompletableFuture;

public interface FilterChain {

    CompletableFuture<TarsServantResponse> doFilter(Request request) throws Throwable;
}
