package com.qq.tars.register.eureka;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.lang.reflect.Field;

public class TarsEurekaInstanceTest {

    @Test
    public void testAfterPropertiesSetWillPopulateMetadata() throws Exception {
        TarsEurekaInstance instance = new TarsEurekaInstance(new InetUtils(new InetUtilsProperties()));
        instance.setNonSecurePort(18601);
        setField(instance, "isTcp", 1);
        setField(instance, "timeOut", 7000);
        setField(instance, "weight", 18);
        setField(instance, "weightType", 2);

        instance.afterPropertiesSet();

        Assert.assertEquals("1", instance.getMetadataMap().get("isTcp"));
        Assert.assertEquals("7000", instance.getMetadataMap().get("timeOut"));
        Assert.assertEquals("18", instance.getMetadataMap().get("weight"));
        Assert.assertEquals("2", instance.getMetadataMap().get("weightType"));
        Assert.assertEquals(instance.getIpAddress() + ":18601", instance.getMetadataMap().get("instanceId"));
    }

    @Test
    public void testSetAppnameWithAppAndServer() {
        TarsEurekaInstance instance = new TarsEurekaInstance(new InetUtils(new InetUtilsProperties()));
        instance.setAppname("DemoApp.DemoServer");
        Assert.assertEquals("DemoApp.DemoServer", instance.getAppname());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = TarsEurekaInstance.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
