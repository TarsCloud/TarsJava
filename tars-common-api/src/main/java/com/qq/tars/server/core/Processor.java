package com.qq.tars.server.core;

import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import io.netty.channel.Channel;

public interface Processor {
    /**
     * @param request
     * @param clientChannel
     * @return
     */
    Response process(Request request, Channel clientChannel);

    /**
     * @param request
     * @param clientChannel
     */
    void overload(Request request, Channel clientChannel);
}
