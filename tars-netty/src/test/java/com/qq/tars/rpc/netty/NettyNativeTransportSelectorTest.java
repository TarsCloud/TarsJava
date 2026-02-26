package com.qq.tars.rpc.netty;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;
import org.junit.Assert;
import org.junit.Test;

public class NettyNativeTransportSelectorTest {

    @Test
    public void testSelectEpollFirst() {
        Assert.assertEquals(NettyNativeTransportSelector.Transport.EPOLL,
                NettyNativeTransportSelector.select(true, true));
        Assert.assertEquals(NettyNativeTransportSelector.Transport.EPOLL,
                NettyNativeTransportSelector.select(true, false));
    }

    @Test
    public void testSelectKQueueWhenEpollUnavailable() {
        Assert.assertEquals(NettyNativeTransportSelector.Transport.KQUEUE,
                NettyNativeTransportSelector.select(false, true));
    }

    @Test
    public void testSelectNioFallback() {
        Assert.assertEquals(NettyNativeTransportSelector.Transport.NIO,
                NettyNativeTransportSelector.select(false, false));
    }

    @Test
    public void testCurrentTransportShouldMatchRuntimeAvailability() {
        NettyNativeTransportSelector.Transport expected = NettyNativeTransportSelector.select(
                Epoll.isAvailable(), KQueue.isAvailable());
        NettyNativeTransportSelector.Transport current = NettyNativeTransportSelector.current();
        Assert.assertEquals(expected, current);
        if (current == NettyNativeTransportSelector.Transport.NIO) {
            Assert.assertNotNull(NettyNativeTransportSelector.unavailableCause());
            Assert.assertFalse(NettyNativeTransportSelector.unavailableCause().isEmpty());
        }
    }
}
