package com.qq.tars.server.core;

import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import io.netty.channel.Channel;

public interface Processor {
    /***
     *
     * @param req
     * @param clientChannel
     * @return
     */
    Response process(Request req, Channel clientChannel);

    /****
     *
     * @param req
     * @param session
     *
     */
    void overload(Request req, Channel session);
}
