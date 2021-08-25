package com.qq.tars.client.subset;

import com.qq.tars.common.support.Holder;
import com.qq.tars.common.util.DyeingSwitch;
import com.qq.tars.context.DistributedContext;
import com.qq.tars.context.impl.DistributedContextImpl;
import com.qq.tars.net.core.Session;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.support.query.prx.EndpointF;
import org.junit.Test;

import java.time.Instant;
import java.util.*;

/**
 * @author dlhjw
 * @version 1.0
 * @date 2021/8/24 21:03
 */
public class TestSubset {

    //创建Subset过滤器
    Subset subsetFilter = new Subset();

    //模拟objectName
    String objectName = "objectName";

    //模拟routeKey
    String routeKey = "routeKey";

    //存活节点list列表
    List<EndpointF> endpointFList = new ArrayList<EndpointF>();
    Holder<List<EndpointF>> activeEp = new Holder<List<EndpointF>>(new ArrayList<EndpointF>());

    //定义一个Session域，用来构建Tars请求体
    Session session;


    /**
     * 按比例路由规则 - 单次测试
     * 没有测试registry获取subsetConf功能
     */
    @Test
    public void testRatioOnce() {

        //1. 给过滤器设置过滤规则
        //1.1 创建SubsetManager管理器
        SubsetManager subsetManager = new SubsetManager();


        //1.1 设置比例路由规则
        RatioConfig ratioConf = new RatioConfig();
        Map<String , Integer> map = new HashMap<>();
        map.put("v1",20);
        map.put("v2",80);
        //map.put("v3",20);
        ratioConf.setRules(map);

        //1.2 设置subsetConf，并加入缓存
        SubsetConf subsetConf = new SubsetConf();
        subsetConf.setEnanle(true);
        subsetConf.setRuleType("ratio");
        subsetConf.setRatioConf(ratioConf);
        subsetConf.setLastUpdate( Instant.now() );

        Map<String, SubsetConf> cache = new HashMap<>();
        cache.put(objectName,subsetConf);
        subsetManager.setCache(cache);

        //1.3 给过滤器设置过滤规则和管理者
        subsetFilter.setSubsetConf(subsetConf);
        subsetFilter.setSubsetManager(subsetManager);


        //2. 模拟存活节点
        endpointFList.add(new EndpointF("host1",1,2,3,4,5,6,"setId1",7,8,9,10,"v1"));
        endpointFList.add(new EndpointF("host2",1,2,3,4,5,6,"setId2",7,8,9,10,"v1"));
        endpointFList.add(new EndpointF("host3",1,2,3,4,5,6,"setId3",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host4",1,2,3,4,5,6,"setId4",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host5",1,2,3,4,5,6,"setId5",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host5",1,2,3,4,5,6,"setId5",7,8,9,10,"v3"));
        activeEp.setValue(endpointFList);


        //3. 输出过滤前信息
        System.out.println("过滤前节点信息如下：");
        for( EndpointF endpoint : endpointFList){
            System.out.println(endpoint.toString());
        }

        //4. 对存活节点按subset规则过滤
        Holder<List<EndpointF>> filterActiveEp = subsetFilter.subsetEndpointFilter(objectName, routeKey, activeEp);

        //5. 输出过滤结果

        System.out.println("过滤后节点信息如下：");
        for( EndpointF endpoint : filterActiveEp.getValue() ){
            System.out.println(endpoint.toString());
        }
    }


    /**
     * 按比例路由规则 - 多次测试
     * 没有测试registry获取subsetConf功能
     */
    @Test
    public void testRatioTimes() {

        //1. 给过滤器设置过滤规则
        //1.1 创建SubsetManager管理器
        SubsetManager subsetManager = new SubsetManager();


        //1.1 设置比例路由规则
        RatioConfig ratioConf = new RatioConfig();
        Map<String , Integer> map = new HashMap<>();
        map.put("v1",20);
        map.put("v2",80);
        map.put("v3",20);
        ratioConf.setRules(map);

        //1.2 设置subsetConf，并加入缓存
        SubsetConf subsetConf = new SubsetConf();
        subsetConf.setEnanle(true);
        subsetConf.setRuleType("ratio");
        subsetConf.setRatioConf(ratioConf);
        subsetConf.setLastUpdate( Instant.now() );

        Map<String, SubsetConf> cache = new HashMap<>();
        cache.put(objectName,subsetConf);
        subsetManager.setCache(cache);

        //1.3 给过滤器设置过滤规则和管理者
        subsetFilter.setSubsetConf(subsetConf);
        subsetFilter.setSubsetManager(subsetManager);


        //2. 模拟存活节点
        endpointFList.add(new EndpointF("host1",1,2,3,4,5,6,"setId1",7,8,9,10,"v1"));
        endpointFList.add(new EndpointF("host2",1,2,3,4,5,6,"setId2",7,8,9,10,"v1"));
        endpointFList.add(new EndpointF("host3",1,2,3,4,5,6,"setId3",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host4",1,2,3,4,5,6,"setId4",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host5",1,2,3,4,5,6,"setId5",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host5",1,2,3,4,5,6,"setId5",7,8,9,10,"v3"));
        activeEp.setValue(endpointFList);


        //3. 循环times次
        int times = 1000000;
        int v1Times = 0;
        int v2Times = 0;
        int v3Times = 0;
        int errTimes = 0;
        for (int i = 0; i < times; i++) {
            //对存活节点按subset规则过滤
            Holder<List<EndpointF>> filterActiveEp = subsetFilter.subsetEndpointFilter(objectName, routeKey, activeEp);
            String subsetValue = filterActiveEp.getValue().get(0).getSubset();
            if("v1".equals(subsetValue)){
                v1Times++;
            } else if("v2".equals(subsetValue)){
                v2Times++;
            } else if("v3".equals(subsetValue)){
                v3Times++;
            } else {
                errTimes++;
            }

        }
        //输出结果
        System.out.println("一共循环次数：" + times);
        System.out.println("路由到v1次数：" + v1Times);
        System.out.println("路由到v2次数：" + v2Times);
        System.out.println("路由到v3次数：" + v3Times);
        System.out.println("路由异常次数：" + errTimes);
    }


    /**
     * 测试参数匹配 - 精确匹配
     * 没有测试registry获取subsetConf功能
     * 注意要成功必须routeKey和match匹配上
     */
    @Test
    public void testMatch() {

        //1. 给过滤器设置过滤规则
        //1.1 创建SubsetManager管理器
        SubsetManager subsetManager = new SubsetManager();


        //1.1 设置参数路由规则，这里的KeyRoute的value为 “规则的染色key”
        KeyConfig keyConf = new KeyConfig();
        List<KeyRoute> krs = new LinkedList<>();
        krs.add(new KeyRoute("match","routeKey","v1"));
        keyConf.setRules(krs);

        //1.2 设置subsetConf，并加入缓存
        SubsetConf subsetConf = new SubsetConf();
        subsetConf.setEnanle(true);
        subsetConf.setRuleType("key");
        subsetConf.setKeyConf(keyConf);
        subsetConf.setLastUpdate( Instant.now() );

        Map<String, SubsetConf> cache = new HashMap<>();
        cache.put(objectName,subsetConf);
        subsetManager.setCache(cache);

        //1.3 给过滤器设置过滤规则和管理者
        subsetFilter.setSubsetConf(subsetConf);
        subsetFilter.setSubsetManager(subsetManager);

        //1.4 模拟Tars “请求的染色key” TARS_ROUTE_KEY，但请求染色key和规则染色key匹配时，才能精确路由
        //1.4.1 创建Tars的请求体TarsServantRequest
        TarsServantRequest request = new TarsServantRequest( session );
        //1.4.2 往请求体的status添加{TARS_ROUTE_KEY, "routeKey"}键值对
        Map<String, String> status = new HashMap<>();
        status.put("TARS_ROUTE_KEY", "routeKey");
        request.setStatus(status);
        //1.4.3 构建分布式上下文信息，将请求放入分布式上下文信息中，因为getSubset()的逻辑是从分布式上下文信息中取
        DistributedContext distributedContext = new DistributedContextImpl();
        distributedContext.put(DyeingSwitch.REQ,request);

        //2. 模拟存活节点
        endpointFList.add(new EndpointF("host1",1,2,3,4,5,6,"setId1",7,8,9,10,"v1"));
        endpointFList.add(new EndpointF("host2",1,2,3,4,5,6,"setId2",7,8,9,10,"v1"));
        endpointFList.add(new EndpointF("host3",1,2,3,4,5,6,"setId3",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host4",1,2,3,4,5,6,"setId4",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host5",1,2,3,4,5,6,"setId5",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host5",1,2,3,4,5,6,"setId5",7,8,9,10,"v3"));
        activeEp.setValue(endpointFList);


        //3. 输出过滤前信息
        System.out.println("过滤前节点信息如下：");
        for( EndpointF endpoint : endpointFList){
            System.out.println(endpoint.toString());
        }

        //4. 对存活节点按subset规则过滤
        Holder<List<EndpointF>> filterActiveEp = subsetFilter.subsetEndpointFilter(objectName, routeKey, activeEp);

        //5. 输出过滤结果

        System.out.println("过滤后节点信息如下：");
        for( EndpointF endpoint : filterActiveEp.getValue() ){
            System.out.println(endpoint.toString());
        }
    }


    /**
     * 测试参数匹配 - 正则匹配
     * 没有测试registry获取subsetConf功能
     * 注意要成功必须routeKey和match匹配上
     */
    @Test
    public void testEqual() {

        //1. 给过滤器设置过滤规则
        //1.1 创建SubsetManager管理器
        SubsetManager subsetManager = new SubsetManager();


        //1.1 设置参数路由规则，这里的KeyRoute的value为 “规则的染色key”
        KeyConfig keyConf = new KeyConfig();
        List<KeyRoute> krs = new LinkedList<>();
        krs.add(new KeyRoute("equal","routeKey","v1"));
        keyConf.setRules(krs);

        //1.2 设置subsetConf，并加入缓存
        SubsetConf subsetConf = new SubsetConf();
        subsetConf.setEnanle(true);
        subsetConf.setRuleType("key");
        subsetConf.setKeyConf(keyConf);
        subsetConf.setLastUpdate( Instant.now() );

        Map<String, SubsetConf> cache = new HashMap<>();
        cache.put(objectName,subsetConf);
        subsetManager.setCache(cache);

        //1.3 给过滤器设置过滤规则和管理者
        subsetFilter.setSubsetConf(subsetConf);
        subsetFilter.setSubsetManager(subsetManager);

        //1.4 模拟Tars “请求的染色key” TARS_ROUTE_KEY，但请求染色key和规则染色key匹配时，才能精确路由
        //1.4.1 创建Tars的请求体TarsServantRequest
        TarsServantRequest request = new TarsServantRequest( session );
        //1.4.2 往请求体的status添加{TARS_ROUTE_KEY, "routeKey"}键值对
        Map<String, String> status = new HashMap<>();
        status.put("TARS_ROUTE_KEY", "route*");
        request.setStatus(status);
        //1.4.3 构建分布式上下文信息，将请求放入分布式上下文信息中，因为getSubset()的逻辑是从分布式上下文信息中取
        DistributedContext distributedContext = new DistributedContextImpl();
        distributedContext.put(DyeingSwitch.REQ,request);

        //2. 模拟存活节点
        endpointFList.add(new EndpointF("host1",1,2,3,4,5,6,"setId1",7,8,9,10,"v1"));
        endpointFList.add(new EndpointF("host2",1,2,3,4,5,6,"setId2",7,8,9,10,"v1"));
        endpointFList.add(new EndpointF("host3",1,2,3,4,5,6,"setId3",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host4",1,2,3,4,5,6,"setId4",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host5",1,2,3,4,5,6,"setId5",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host5",1,2,3,4,5,6,"setId5",7,8,9,10,"v3"));
        activeEp.setValue(endpointFList);


        //3. 输出过滤前信息
        System.out.println("过滤前节点信息如下：");
        for( EndpointF endpoint : endpointFList){
            System.out.println(endpoint.toString());
        }

        //4. 对存活节点按subset规则过滤
        Holder<List<EndpointF>> filterActiveEp = subsetFilter.subsetEndpointFilter(objectName, routeKey, activeEp);

        //5. 输出过滤结果

        System.out.println("过滤后节点信息如下：");
        for( EndpointF endpoint : filterActiveEp.getValue() ){
            System.out.println(endpoint.toString());
        }
    }



    /**
     *  registry测试
     */
    @Test
    public void testCache() throws InterruptedException {

        //1. 给过滤器设置过滤规则
        //1.1 创建SubsetManager管理器
        SubsetManager subsetManager = new SubsetManager();


        //1.1 设置比例路由规则
        RatioConfig ratioConf = new RatioConfig();
        Map<String , Integer> map = new HashMap<>();
        map.put("v1",20);
        map.put("v2",80);
        //map.put("v3",20);
        ratioConf.setRules(map);

        //1.2 设置subsetConf，并加入缓存
        SubsetConf subsetConf = new SubsetConf();
        subsetConf.setEnanle(true);
        subsetConf.setRuleType("ratio");
        subsetConf.setRatioConf(ratioConf);
        subsetConf.setLastUpdate( Instant.now() );
        //1.3 延迟10s，使缓存的subsetConf失效
        Thread.sleep(1000);

        Map<String, SubsetConf> cache = new HashMap<>();
        cache.put(objectName,subsetConf);
        subsetManager.setCache(cache);

        //1.3 给过滤器设置过滤规则和管理者
        subsetFilter.setSubsetConf(subsetConf);
        subsetFilter.setSubsetManager(subsetManager);

        //2. 模拟存活节点
        endpointFList.add(new EndpointF("host1",1,2,3,4,5,6,"setId1",7,8,9,10,"v1"));
        endpointFList.add(new EndpointF("host2",1,2,3,4,5,6,"setId2",7,8,9,10,"v1"));
        endpointFList.add(new EndpointF("host3",1,2,3,4,5,6,"setId3",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host4",1,2,3,4,5,6,"setId4",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host5",1,2,3,4,5,6,"setId5",7,8,9,10,"v2"));
        endpointFList.add(new EndpointF("host5",1,2,3,4,5,6,"setId5",7,8,9,10,"v3"));
        activeEp.setValue(endpointFList);


        //3. 输出过滤前信息
        System.out.println("过滤前节点信息如下：");
        for( EndpointF endpoint : endpointFList){
            System.out.println(endpoint.toString());
        }

        //4. 对存活节点按subset规则过滤
        Holder<List<EndpointF>> filterActiveEp = subsetFilter.subsetEndpointFilter(objectName, routeKey, activeEp);

        //5. 输出过滤结果

        System.out.println("过滤后节点信息如下：");
        for( EndpointF endpoint : filterActiveEp.getValue() ){
            System.out.println(endpoint.toString());
        }
    }



}
