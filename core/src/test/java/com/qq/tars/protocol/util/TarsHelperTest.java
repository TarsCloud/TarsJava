package com.qq.tars.protocol.util;

import com.qq.tars.protocol.annotation.Servant;
import com.qq.tars.protocol.tars.support.TarsMethodInfo;
import com.qq.tars.protocol.tars.support.TarsMethodParameterInfo;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TarsHelperTest {

    @Test
    public void getMethodInfoWithServant() throws Exception {
        Class<ApiServant> api = ApiServant.class;
        Map<Method, TarsMethodInfo> methodInfoMap = TarsHelper.getMethodInfo(api, null, "test.app.objName");
        Method get = api.getDeclaredMethod("get", int.class, String.class, Req.class);

        TarsMethodInfo methodInfo = methodInfoMap.get(get);
        assertNotNull(methodInfo);
        assertEquals(get, methodInfo.getMethod());
        List<TarsMethodParameterInfo> parametersList = methodInfo.getParametersList();
        assertEquals(3, parametersList.size());
        assertEquals("args0", parametersList.get(0).getName());
        assertEquals("args1", parametersList.get(1).getName());
        assertEquals("args2", parametersList.get(2).getName());
    }

    @Test
    public void getMethodInfoWithServantImpl() throws Exception {
        Class<ApiServant> api = ApiServant.class;
        Map<Method, TarsMethodInfo> methodInfoMap = TarsHelper.getMethodInfo(api, new ApiServantImpl(), "test.app.objName");
        Method get = api.getDeclaredMethod("get", int.class, String.class, Req.class);

        TarsMethodInfo methodInfo = methodInfoMap.get(get);
        assertNotNull(methodInfo);
        assertEquals(get, methodInfo.getMethod());
        List<TarsMethodParameterInfo> parametersList = methodInfo.getParametersList();
        assertEquals(3, parametersList.size());
        assertEquals("no", parametersList.get(0).getName());
        assertEquals("name", parametersList.get(1).getName());
        assertEquals("request", parametersList.get(2).getName());
    }
}

@Servant
interface ApiServant {
    Resp get(int no, String name, Req request);
}

class ApiServantImpl implements ApiServant {

    @Override
    public Resp get(int no, String name, Req request) {
        Resp resp = new Resp();
        resp.retCode = 0;
        resp.desc = "ok";
        return resp;
    }
}

class Req {
    int a;
    long b;
    String s;
}

class Resp {
    int retCode;
    String desc;
    Map data;
}