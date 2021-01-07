package com.qq.tars.client.rpc;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.rpc.common.Url;

public class NettyClientTransporter {

    public static NettyServantClient connect(Url url, ServantProxyConfig servantProxyConfig, ChannelHandler handler) {
        return new NettyServantClient(url, servantProxyConfig, handler);
    }
}
