package com.qq.tars.rpc.netty;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.rpc.*;
import com.qq.tars.rpc.common.Url;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.core.Processor;
import com.qq.tars.support.log.LoggerFactory;
import io.netty.channel.Channel;
import org.slf4j.Logger;

/**
 * The netty implementation of TransporterFactory.
 * Used to connect to a tars server node(client-side) or listen an ip:port(server-side)
 *
 * @author TimmyYu
 * @author kongyuanyuan
 */
public class NettyTransporterFactory implements TransporterFactory {
    /**
     * Connect with a server node using the given servant config.
     *
     * @param url                server node url
     * @param servantProxyConfig servant config
     * @return rpc client instance
     */
    @Override
    public RPCClient connect(Url url, ServantProxyConfig servantProxyConfig) {
        return connect(url, servantProxyConfig, new InnerDefaultClientHandler());
    }

    /**
     * Connect with a server node using the given servant config and channel handler.
     *
     * @param url                server node url
     * @param servantProxyConfig servant config
     * @param channelHandler     custom channel handler. Can be used in unit tests.
     * @return rpc client instance
     */
    @Override
    public NettyServantClient connect(Url url, ServantProxyConfig servantProxyConfig, ChannelHandler channelHandler) {
        return new NettyServantClient(url, servantProxyConfig, channelHandler);
    }

    /**
     * Get the server instance for transportation which can be used to bind ip/port with {@link TransporterServer#bind()} method.
     *
     * @param servantAdapterConfig the servant adapter config which is provided by the platform.
     * @param processor            the request processor.
     * @return transporter server instance
     * @see com.qq.tars.server.core.Processor
     */
    @Override
    public TransporterServer getTransporterServer(ServantAdapterConfig servantAdapterConfig, Processor processor) {
        return new NettyTransporterServer(servantAdapterConfig, new InnerDefaultServerHandler(processor));
    }

    private static class InnerDefaultClientHandler implements ChannelHandler {
        private static final Logger logger = LoggerFactory.getClientLogger();

        @Override
        public void connected(Channel channel) {

        }

        @Override
        public void disconnected(Channel channel) {

        }

        @Override
        public void send(Channel channel, Object message) {

        }

        @Override
        public void received(Channel channel, Object message) {
            TarsServantResponse response = (TarsServantResponse) message;
            if (logger.isDebugEnabled()) {
                logger.debug("[tars]netty receive message id is {}", response.getRequestId());
            }
            TicketFeature.getFeature(response.getRequestId()).complete(response);
        }

        @Override
        public void caught(Channel channel, Throwable exception) {

        }

        @Override
        public void destroy() {

        }
    }

    private static class InnerDefaultServerHandler implements ChannelHandler {

        public final Processor processor;

        public InnerDefaultServerHandler(Processor processor) {
            this.processor = processor;
        }

        @Override
        public void connected(Channel channel) {

        }

        @Override
        public void disconnected(Channel channel) {

        }

        @Override
        public void send(Channel channel, Object message) {

        }

        @Override
        public void received(Channel channel, Object message) {
            Response response = processor.process((Request) message, channel);
            if (!response.isAsyncMode() && channel.isWritable()) {
                channel.writeAndFlush(response);
            }
        }

        @Override
        public void caught(Channel channel, Throwable exception) {

        }

        @Override
        public void destroy() {

        }
    }
}
