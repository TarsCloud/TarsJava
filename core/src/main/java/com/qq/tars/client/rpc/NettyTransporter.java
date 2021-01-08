package com.qq.tars.client.rpc;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.rpc.common.Url;
import com.qq.tars.server.config.ServantAdapterConfig;

public class NettyTransporter {

    public static NettyServantClient connect(Url url, ServantProxyConfig servantProxyConfig, ChannelHandler handler) {
        return new NettyServantClient(url, servantProxyConfig, handler);
    }


    public static NettyServer bind(ServantAdapterConfig servantAdapterConfig, ChannelHandler handler) {
        return new NettyServer(servantAdapterConfig, handler);
    }
}
