package com.qq.tars.rpc.netty;

import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import com.qq.tars.client.rpc.TransporterServer;
import com.qq.tars.common.support.Endpoint;
import com.qq.tars.common.util.VirtualThreadSupport;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.core.Processor;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class NettyTransporterFactoryVirtualThreadTest {

    @Test
    public void testReceivedRunsInCallerThreadWhenVirtualThreadDisabled() throws Exception {
        String oldGlobal = System.getProperty(VirtualThreadSupport.VTHREAD_ENABLED_KEY);
        String oldServer = System.getProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY);
        try {
            System.clearProperty(VirtualThreadSupport.VTHREAD_ENABLED_KEY);
            System.clearProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY);

            AtomicReference<Thread> executedThread = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);
            ChannelHandler handler = createServerHandler(createProcessor(executedThread, latch));
            EmbeddedChannel channel = new EmbeddedChannel();
            Thread callerThread = Thread.currentThread();

            handler.received(channel, new DummyRequest(10086));

            Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));
            Assert.assertSame(callerThread, executedThread.get());
            Assert.assertTrue(waitOutbound(channel, 1000) instanceof Response);
            channel.finishAndReleaseAll();
        } finally {
            restoreProperty(VirtualThreadSupport.VTHREAD_ENABLED_KEY, oldGlobal);
            restoreProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY, oldServer);
        }
    }

    @Test
    public void testReceivedCanRunOnVirtualThreadWhenEnabled() throws Exception {
        String oldGlobal = System.getProperty(VirtualThreadSupport.VTHREAD_ENABLED_KEY);
        String oldServer = System.getProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY);
        try {
            System.setProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY, "true");

            AtomicReference<Thread> executedThread = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);
            ChannelHandler handler = createServerHandler(createProcessor(executedThread, latch));
            EmbeddedChannel channel = new EmbeddedChannel();
            Thread callerThread = Thread.currentThread();

            handler.received(channel, new DummyRequest(10010));

            Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));
            if (VirtualThreadSupport.isVirtualThreadSupported()) {
                Assert.assertNotSame(callerThread, executedThread.get());
                Assert.assertTrue(isVirtualThread(executedThread.get()));
            } else {
                Assert.assertSame(callerThread, executedThread.get());
            }
            Assert.assertTrue(waitOutbound(channel, 1000) instanceof Response);
            channel.finishAndReleaseAll();
        } finally {
            restoreProperty(VirtualThreadSupport.VTHREAD_ENABLED_KEY, oldGlobal);
            restoreProperty(VirtualThreadSupport.SERVER_VTHREAD_ENABLED_KEY, oldServer);
        }
    }

    private static Processor createProcessor(AtomicReference<Thread> executedThread, CountDownLatch latch) {
        return new Processor() {
            @Override
            public Response process(Request request, io.netty.channel.Channel clientChannel) {
                executedThread.set(Thread.currentThread());
                latch.countDown();
                return new TarsServantResponse(request.getRequestId());
            }

            @Override
            public void overload(Request request, io.netty.channel.Channel clientChannel) {
            }
        };
    }

    private static ChannelHandler createServerHandler(Processor processor) throws Exception {
        ServantAdapterConfig config = ServantAdapterConfig.makeServantAdapterConfig(
                Endpoint.parseString("tcp -h 127.0.0.1 -p 18601 -t 60000"),
                "TestApp.TestServer.TestObj", null);
        TransporterServer transporterServer = new NettyTransporterFactory().getTransporterServer(config, processor);
        Field channelHandlerField = NettyTransporterServer.class.getDeclaredField("channelHandler");
        channelHandlerField.setAccessible(true);
        return (ChannelHandler) channelHandlerField.get(transporterServer);
    }

    private static Object waitOutbound(EmbeddedChannel channel, long timeoutMillis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        Object outbound = channel.readOutbound();
        while (outbound == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
            outbound = channel.readOutbound();
        }
        return outbound;
    }

    private static boolean isVirtualThread(Thread thread) throws Exception {
        if (thread == null) {
            return false;
        }
        Method method = Thread.class.getMethod("isVirtual");
        return (Boolean) method.invoke(thread);
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    private static class DummyRequest implements Request {
        private final int requestId;

        private DummyRequest(int requestId) {
            this.requestId = requestId;
        }

        @Override
        public int getRequestId() {
            return requestId;
        }

        @Override
        public String getServantName() {
            return "TestApp.TestServer.TestObj";
        }

        @Override
        public String getFunctionName() {
            return "test";
        }

        @Override
        public Map<String, String> getDistributedContext() {
            return Collections.emptyMap();
        }

        @Override
        public long getProcessTime() {
            return 0;
        }

        @Override
        public long getBornTime() {
            return System.currentTimeMillis();
        }
    }
}
