package com.qq.tars.client.rpc;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.rpc.common.Url;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.core.Processor;

/**
 * Abstract factory for creating servers and clients in transportation。
 *
 * @author kongyuanyuan
 */
public interface TransporterFactory {

    /**
     * Connect with a server node using the given servant config.
     *
     * @param url                server node url
     * @param servantProxyConfig servant config
     * @return rpc client instance
     */
    RPCClient connect(Url url, ServantProxyConfig servantProxyConfig);

    /**
     * Connect with a server node using the given servant config and channel handler.
     *
     * @param url                server node url
     * @param servantProxyConfig servant config
     * @param channelHandler     custom channel handler. Can be used in unit tests.
     * @return rpc client instance
     */
    RPCClient connect(Url url, ServantProxyConfig servantProxyConfig, ChannelHandler channelHandler);

    /**
     * Get the server instance for transportation which can be used to bind ip/port with {@link TransporterServer#bind()} method.
     *
     * @param servantAdapterConfig the servant adapter config which is provided by the platform.
     * @param processor            the request processor.
     * @return transporter server instance
     * @see com.qq.tars.server.core.Processor
     */
    TransporterServer getTransporterServer(ServantAdapterConfig servantAdapterConfig, Processor processor);
}
