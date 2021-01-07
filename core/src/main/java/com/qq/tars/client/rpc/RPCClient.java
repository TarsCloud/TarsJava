package com.qq.tars.client.rpc;

import com.qq.tars.rpc.protocol.ServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import io.netty.channel.Channel;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


public interface RPCClient {


    /**
     * @return return ioChannel
     */
    Channel getChannel();

    /**
     * send object
     * @param request tarsServantRequest Object
     * @return response Future
     * @throws IOException
     */
    CompletableFuture<TarsServantResponse> send(ServantRequest request) throws IOException;


    /**
     * @throws IOException when close Exception
     */
    void close() throws IOException;

}
