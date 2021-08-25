package com.qq.tars.client.subset;


import java.util.Map;
import java.util.Random;
import java.util.Set;

public class RatioConfig {

    private Map<String, Integer> rules;


    //进行路由规则的具体实现，返回subset字段
    public String findSubet(String routeKey){
        //routeKey为空时随机
        if( "".equals(routeKey) ){
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

        //routeKey不为空时实现按比例算法
        int totalWeight = 0;
        int supWeight = 0;
        String subset = null;
        //获得总权重
        for (Integer value : rules.values()) {
            totalWeight+=value;
        }
        //获取随机数
        Random random = new Random();
        int r = random.nextInt(totalWeight);
        //根据随机数找到subset
        for (Map.Entry<String, Integer> entry : rules.entrySet()){
            supWeight+=entry.getValue();
            if( r < supWeight){
                subset = entry.getKey();
                return subset;
            }
        }
        return null;
    }

    public Map<String, Integer> getRules() {
        return rules;
    }

    public void setRules(Map<String, Integer> rules) {
        this.rules = rules;
    }
}
