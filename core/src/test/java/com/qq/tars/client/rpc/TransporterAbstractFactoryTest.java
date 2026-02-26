package com.qq.tars.client.rpc;

import com.qq.tars.rpc.netty.NettyTransporterFactory;
import org.junit.Assert;
import org.junit.Test;

public class TransporterAbstractFactoryTest {

    @Test
    public void testSingletonInstance() {
        Assert.assertSame(TransporterAbstractFactory.getInstance(), TransporterAbstractFactory.getInstance());
    }

    @Test
    public void testUseNettyTransporterFactory() {
        TransporterFactory transporterFactory = TransporterAbstractFactory.getInstance().getTransporterFactory();
        Assert.assertNotNull(transporterFactory);
        Assert.assertEquals(NettyTransporterFactory.class, transporterFactory.getClass());
    }
}
