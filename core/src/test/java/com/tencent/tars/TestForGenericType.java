package com.tencent.tars;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import junit.framework.Assert;
import org.junit.Test;

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
        Type returnType = method.getGenericReturnType();
        Type rawReturnType = method.getReturnType();

        // 确保返回类型是 CompletableFuture
        Assert.assertEquals(rawReturnType, CompletableFuture.class);

        // 处理泛型返回类型
        if (returnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) returnType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Assert.assertEquals(actualTypeArguments[0].getTypeName(), "java.util.List<java.lang.Integer>");

            // 打印信息
            System.out.println(actualTypeArguments[0].getTypeName());
            System.out.println("get inner type is " + actualTypeArguments[0]);
            System.out.println("return type is " + method.getGenericReturnType());
            // 调用方法
            System.out.println(method.invoke(TestsMethod.class.newInstance()));
        } else {
            System.out.println("Return type is not a ParameterizedType");
        }

        System.out.println(completableFuture.getClass().getName());
    }
}
