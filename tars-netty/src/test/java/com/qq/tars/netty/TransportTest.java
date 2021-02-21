package com.qq.tars.netty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.CommunicatorFactory;
import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.rpc.*;
import com.qq.tars.common.support.Holder;
import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.common.Url;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.rpc.protocol.tars.support.AnalystManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class TransportTest {
    @Test
    @Ignore
    public void testForConnection() throws Exception {

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
                System.out.println("receive message " + ((TarsServantResponse) message).getResult());

            }

            @Override
            public void caught(Channel channel, Throwable exception) {

            }

            @Override
            public void destroy() {

            }
        };
        ServantProxyConfig servantProxyConfig = new ServantProxyConfig("test");

        AnalystManager.getInstance().registry(MonitorQueryPrx.class, "tars.tarsquerystat.QueryObj");
        //Url(String protocol, String host, int port, String path, Map<String, String> parameters) {
        //tcp -h 10.172.0.111 -t 60000 -p 18393
        Url url = new Url("tcp", "10.172.0.111", 18393);
        long startTime = System.nanoTime();
        RPCClient client = TransporterAbstractFactory.getInstance().getTransporterFactory().connect(url, servantProxyConfig, channelHandler);
        System.out.println("init channel time " + ((System.nanoTime() - startTime) / 1000));
        for (int i = 0; i < 100; i++) {
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
            tarsServantRequest.setContext(Maps.newHashMap());
            tarsServantRequest.setStatus(Maps.newHashMap());
            tarsServantRequest.setPacketType(TarsHelper.NORMAL);
            System.out.println("client connection is " + client.getChannel().isActive());
            System.out.println("client connection is " + client.getChannel().isOpen());
            System.out.println("client connection is " + client.getChannel().isWritable());
            long startTimeNano = System.currentTimeMillis();
            CompletableFuture<Response> responseCompletableFuture = client.send(tarsServantRequest).whenComplete((x, y) -> {
                System.out.print(System.currentTimeMillis() - startTimeNano + "::");
                System.out.println(response);
            });
        }
        System.out.println("client connection is " + client.getChannel().isActive());
        System.out.println("client connection is " + client.getChannel().isOpen());
        System.out.println("client connection is " + client.getChannel().isWritable());


        Thread.sleep(10000);
    }


    @Test
    public void testForEncoder() {
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


    @Test
    public void maxCappcity() {
        ByteBuf buffer = Unpooled.directBuffer();
        buffer.writeInt(2014);
        buffer.writeInt(2014);
        buffer.writeInt(2014);
        buffer.writeInt(2014);
        buffer.writeInt(2014);
        System.out.println(buffer.getInt(4 * 5));
    }


    @Test
    public void testForQuery() {
        CommunicatorConfig config = new CommunicatorConfig();
        MonitorQueryPrx queryPrx = CommunicatorFactory.getInstance().getCommunicator(config).stringToProxy(MonitorQueryPrx.class,
                "tars.tarsquerystat.QueryObj@tcp -h 127.0.0.1 -t 60000 -p 60000");
        MonitorQueryReq request = new MonitorQueryReq();
        request.dateType = DateType.MINIUES.value();
        request.method = "query";
        request.setDataid("tars_stat");
        request.intervalTime = 1;
        request.setConditions(ImmutableList.of(new Condition("slave_name", 5, "tars.tarsstat")));
        request.indexs = ImmutableList.of("succ_count", "timeout_count", "exce_count", "total_time");
        request.setGroupby(ImmutableList.of("f_tflag"));
        request.startTime = 1606060800L;
        request.endTime = 1606147199L;
        System.out.println(request);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Holder<MonitorQueryRsp> holder = new Holder<>();
            queryPrx.query(request, holder);
        }

        System.out.println(System.currentTimeMillis() - startTime);
    }

}
