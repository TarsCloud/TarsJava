package com.tencent.tars;

import java.lang.reflect.ParameterizedType;
import junit.framework.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TestForGenericType {
    public static class TestsMethod {
        public CompletableFuture<List<Integer>> getFuture() {
            return new CompletableFuture<>();
        }
    }

    @Test
    public void testGenerictype() throws InvocationTargetException, IllegalAccessException, InstantiationException {
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        Method[] methods = TestsMethod.class.getMethods();
        Method method = methods[0];
        Type type = method.getGenericReturnType();
        Type type2 = method.getReturnType();
        Assert.assertEquals(type2, CompletableFuture.class);
        ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
        Assert.assertEquals(parameterizedType.getActualTypeArguments()[0].getTypeName(), "java.util.List<java.lang.Integer>");
        System.out.println(parameterizedType.getActualTypeArguments()[0].getTypeName());
        System.out.println("get inner type is " + parameterizedType.getActualTypeArguments()[0]);
        System.out.println("return type is " + method.getGenericReturnType());
        System.out.println(methods[0].invoke(TestsMethod.class.newInstance()));
        System.out.println(completableFuture.getClass().getName());
    }
}
