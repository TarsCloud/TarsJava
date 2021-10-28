package com.qq.tars.client.subset;


import java.util.Map;
import java.util.Random;

public class RatioConfig {

    private Map<String, Integer> rules;
    ConsistentHash consistentHash;


    //进行路由规则的具体实现，返回subset字段
    public String findSubet(String routeKey){
        //routeKey为空时随机
        if( "".equals(routeKey) || routeKey == null ){
            //赋值routeKey为获取的随机值
            Random random = new Random();
            int r = random.nextInt( rules.size() );
            routeKey = String.valueOf(r);
            int i = 0;
            for (String key : rules.keySet()) {
                if(i == r){
                    return key;
                }
                i++;
            }
        }
        //routeKey不为空时实现按比例算法(一致hash)
        return consistentHash.getSubsetByVirtual();
    }

    public Map<String, Integer> getRules() {
        return rules;
    }

    public void setRules(Map<String, Integer> rules) {
        //根据规则创建一致hash的虚拟节点
        ConsistentHash consistentHash = new ConsistentHash();
        consistentHash.setVirtualSubset(rules);
        this.consistentHash = consistentHash;
        this.rules = rules;
    }
}
