/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.qq.tars.rpc.netty;

import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.server.config.ServantAdapterConfig;
import io.netty.channel.Channel;

public class NettyServerChannel {
    private final Channel channel;
    private final ServantAdapterConfig servantProxyConfig;
    private final ChannelHandler channelHandler;

    public NettyServerChannel(Channel channel, ServantAdapterConfig servantProxyConfig, ChannelHandler channelHandler) {
        this.channel = channel;
        this.servantProxyConfig = servantProxyConfig;
        this.channelHandler = channelHandler;

    }

    public Channel getChannel() {
        return channel;
    }

    public ServantAdapterConfig getServantProxyConfig() {
        return servantProxyConfig;
    }

    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    @Override
    public String toString() {
        return "NettyTarsChannel{" +
                "channel=" + channel +
                ", servantProxyConfig=" + servantProxyConfig +
                '}';
    }

}
