package com.qq.tars.netty;

import com.google.common.collect.Maps;
import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.common.support.Endpoint;
import com.qq.tars.common.support.Holder;
import com.qq.tars.rpc.netty.NettyTransporterServer;
import com.qq.tars.rpc.protocol.tars.support.AnalystManager;
import com.qq.tars.server.apps.BaseAppContext;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.server.core.AppContextManager;
import com.qq.tars.server.core.ServantAdapter;
import com.qq.tars.server.core.ServantHomeSkeleton;
import io.netty.channel.Channel;
import org.assertj.core.util.Lists;
import org.junit.Ignore;
import org.junit.Test;

public class TestForNettyTransporterServer {

    @Test
    @Ignore
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
        NettyTransporterServer nettyTransporterServer = new NettyTransporterServer(servantAdapterConfig, channelHandlerer);
        try {
            nettyTransporterServer.bind();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


        Thread.sleep(50000);
    }


    @Test
    public void testForAdapter() throws Throwable {
        Endpoint endpoint = Endpoint.parseString("tcp -h 127.0.0.1 -t 60000 -p 60000");

        AnalystManager.getInstance().registry("", MonitorQueryServant.class, "tars.tarsquerystat.QueryObj");
        ServerConfig serverConfig = new ServerConfig();


        ConfigurationManager.getInstance().setServerConfig(serverConfig);
        ServantAdapterConfig serverAdapterConfig = ServantAdapterConfig.makeServantAdapterConfig(endpoint
                , "tars.tarsquerystat.QueryObj", serverConfig);
        ServantAdapter servantAdapter = new ServantAdapter(serverAdapterConfig);
        ServantHomeSkeleton servantHomeSkeleton = new ServantHomeSkeleton("tars.tarsquerystat.QueryObj",
                new MonitorQuerySeravntImpl(),
                MonitorQueryServant.class,
                0
        );
        BaseAppContext baseAppContext = (new BaseAppContext() {
            @Override
            protected void loadServants() throws Exception {

            }
        });
        baseAppContext.skeletonMap.put("tars.tarsquerystat.QueryObj", servantHomeSkeleton);

        System.out.println(baseAppContext.getCapHomeSkeleton("tars.tarsquerystat.QueryObj"));
        servantHomeSkeleton.setAppContext(baseAppContext);
        servantHomeSkeleton.getAppContext().getCapHomeSkeleton("tars.tarsquerystat.QueryObj");
        AppContextManager.getInstance().setAppContext(servantHomeSkeleton.getAppContext());
        System.out.println(AppContextManager.getInstance().getAppContext() == null);

        serverConfig.getServantAdapterConfMap().put("tars.tarsquerystat.QueryObj", serverAdapterConfig);


        servantAdapter.bind(servantHomeSkeleton);
        Thread.sleep(500000);

    }


    public static class MonitorQuerySeravntImpl implements MonitorQueryServant {

        @Override
        public int query(MonitorQueryReq req, Holder<MonitorQueryRsp> rsp) {
            rsp.setValue(new MonitorQueryRsp());
            rsp.getValue().setResult(Maps.newHashMap());
            rsp.getValue().setRetThreads(Lists.newArrayList());
            rsp.getValue().setMsg("'");
            System.out.println("receive data " + req.toString());
            System.out.println("receive  from client ");
            return 0;
        }
    }
}
