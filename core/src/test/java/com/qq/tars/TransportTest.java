package com.qq.tars;

import com.google.common.collect.ImmutableList;
import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.client.rpc.NettyClientTransport;
import com.qq.tars.client.rpc.NettyServantClient;
import com.qq.tars.client.rpc.Request;
import com.qq.tars.common.support.Holder;
import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.rpc.protocol.tars.support.AnalystManager;
import io.netty.channel.Channel;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class TransportTest {
    @Test
    public void testForConnection() throws Exception {
        ServantProxyConfig servantProxyConfig = new ServantProxyConfig("");

        ChannelHandler channelHandler = new ChannelHandler() {
            @Override
            public void connected(Channel channel) {
                System.out.println("channel header connect");

            }

            @Override
            public void disconnected(Channel channel) {

            }

            @Override
            public void send(Channel channel, Object message) {
                System.out.println(message);

            }

            @Override
            public void received(Channel channel, Object message) {
                System.out.println("receive message " + ((TarsServantResponse)message).getResult());

            }

            @Override
            public void caught(Channel channel, Throwable exception) {

            }

            @Override
            public void destroy() {

            }
        };
        AnalystManager.getInstance().registry(MonitorQueryPrx.class, "tars.tarsquerystat.QueryObj");
        NettyClientTransport nettyClientTransport = new NettyClientTransport(servantProxyConfig, channelHandler);
        nettyClientTransport.init();
        NettyServantClient client = nettyClientTransport.connect("10.172.0.111", 18393);

        MonitorQueryReq monitorQueryReq = new MonitorQueryReq();
        monitorQueryReq.dateType = DateType.MINIUES.value();
        monitorQueryReq.method = "query";
        monitorQueryReq.setDataid("tars_stat");
        monitorQueryReq.intervalTime = 1;
        monitorQueryReq.setConditions(ImmutableList.of(new Condition("slave_name", 5, "tars.tarsstat")));
        monitorQueryReq.indexs = ImmutableList.of("succ_count", "timeout_count", "exce_count", "total_time");
        monitorQueryReq.setGroupby(ImmutableList.of("f_tflag"));
        monitorQueryReq.startTime = 1609171222L;
        monitorQueryReq.endTime = 1609221622L;

        TarsServantRequest tarsServantRequest = new TarsServantRequest();


        Method method = MonitorQueryPrx.class.getMethod("query", MonitorQueryReq.class, Holder.class);
        System.out.println(method);

        tarsServantRequest.setMethodInfo(AnalystManager.getInstance().getMethodMap(MonitorQueryPrx.class).get(method));


        tarsServantRequest.setCharsetName("UTF-8");
        tarsServantRequest.setServantName("tars.tarsquerystat.QueryObj");
        tarsServantRequest.setInvokeStatus(Request.InvokeStatus.SYNC_CALL);
        Holder<MonitorQueryRsp> response = new Holder<>();
        tarsServantRequest.setMethodParameters(new Object[]{monitorQueryReq, response});
        tarsServantRequest.setCharsetName("UTF-8");
        tarsServantRequest.setApi(MonitorQueryPrx.class);
        tarsServantRequest.setFunctionName("query");
        tarsServantRequest.setMessageType(TarsHelper.MESSAGETYPENULL);
        tarsServantRequest.setVersion(TarsHelper.VERSION);
        tarsServantRequest.setContext(new HashMap<>());
        tarsServantRequest.setStatus(new HashMap<>());
        tarsServantRequest.setPacketType(TarsHelper.NORMAL);

        System.out.println(new Object[]{monitorQueryReq, response});

        Thread.sleep(100);
        System.out.println("client connection is " + client.getChannel().isActive());
        System.out.println("client connection is " + client.getChannel().isOpen());
        System.out.println("client connection is " + client.getChannel().isWritable());

        CompletableFuture<TarsServantResponse> responseCompletableFuture = client.send(tarsServantRequest);

        System.out.println("client connection is " + client.getChannel().isActive());
        System.out.println("client connection is " + client.getChannel().isOpen());
        System.out.println("client connection is " + client.getChannel().isWritable());
        System.out.println(responseCompletableFuture.get().getResult());
        System.out.println(response);


    }


    @Test
    public void testForENcoder() {
        MonitorQueryReq monitorQueryReq = new MonitorQueryReq();
        monitorQueryReq.dateType = DateType.MINIUES.value();
        monitorQueryReq.method = "query";
        monitorQueryReq.setDataid("tars_stat");
        monitorQueryReq.intervalTime = 1;
        monitorQueryReq.setConditions(ImmutableList.of(new Condition("slave_name", 5, "tars.tarsstat")));
        monitorQueryReq.indexs = ImmutableList.of("succ_count", "timeout_count", "exce_count", "total_time");
        monitorQueryReq.setGroupby(ImmutableList.of("f_tflag"));
        monitorQueryReq.startTime = 1609171222L;
        monitorQueryReq.endTime = 1609221622L;
        TarsOutputStream tarsOutputStream = new TarsOutputStream();
        monitorQueryReq.writeTo(tarsOutputStream);
        byte[] bytearrsy = tarsOutputStream.toByteArray();
        MonitorQueryReq request2 = new MonitorQueryReq();
        request2.readFrom(new TarsInputStream(bytearrsy));
        System.out.println(monitorQueryReq.getMethod());
        System.out.println(monitorQueryReq.getIndexs());
        System.out.println(monitorQueryReq.getConditions());
        System.out.println("request2" + request2);
    }

}
