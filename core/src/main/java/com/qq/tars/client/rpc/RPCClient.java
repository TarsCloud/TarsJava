package com.qq.tars.client.rpc;

import io.netty.channel.Channel;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


public interface RPCClient {
    /**
     * send object
     * @param request tarsServantRequest Object
     * @return response Future
     * @throws IOException
     */
    CompletableFuture<Response> send(Request request) throws IOException;

    /**
     * @return return ioChannel
     */
    Channel getChannel();

    /**
     * @throws IOException when close Exception
     */
    void close() throws IOException;

}
