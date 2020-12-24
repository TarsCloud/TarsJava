package com.qq.tars.client.rpc;

import com.qq.tars.net.client.Callback;
import com.qq.tars.rpc.protocol.ServantRequest;
import com.qq.tars.rpc.protocol.ServantResponse;

import java.io.IOException;


public interface RPCClient {
    /**
     * @throws IOException
     */
    void reConnect() throws IOException;

    /**
     * @throws IOException
     */
    void ensureConnected() throws IOException;

    /**
     * @param request
     * @throws IOException
     */
    <T extends ServantResponse> T invokeWithSync(ServantRequest request) throws IOException;

    /**
     * @param request
     * @param callback
     * @throws IOException
     */
    <T extends ServantResponse> void invokeWithAsync(ServantRequest request, Callback<T> callback) throws IOException;

    /**
     * @param request
     * @param callback
     * @throws IOException
     */
    <T extends ServantResponse> void invokeWithFuture(ServantRequest request, Callback<T> callback) throws IOException;

}
