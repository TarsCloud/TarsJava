package com.qq.tars;

import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.client.rpc.NettyServer;
import com.qq.tars.common.support.Endpoint;
import com.qq.tars.server.config.ServantAdapterConfig;
import io.netty.channel.Channel;
import org.junit.Test;

public class TestForNettyServer {

    @Test
    public void testForServer() throws InterruptedException {
        ChannelHandler channelHandlerer = new ChannelHandler() {
            @Override
            public void connected(Channel channel) {

                System.out.println("connect from client" + channel);
            }

            @Override
            public void disconnected(Channel channel) {

            }

            @Override
            public void send(Channel channel, Object message) {

            }

            @Override
            public void received(Channel channel, Object message) {

            }

            @Override
            public void caught(Channel channel, Throwable exception) {

            }

            @Override
            public void destroy() {

            }
        };
        Endpoint endpoint = Endpoint.parseString("tcp -h 127.0.0.1 -t 60000 -p 60000");
        ServantAdapterConfig servantAdapterConfig = ServantAdapterConfig.makeServantAdapterConfig(endpoint, "servantConfig", null);
        NettyServer nettyServer = new NettyServer(servantAdapterConfig, channelHandlerer);
        try {
            nettyServer.bind();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


        Thread.sleep(50000);
    }
}
