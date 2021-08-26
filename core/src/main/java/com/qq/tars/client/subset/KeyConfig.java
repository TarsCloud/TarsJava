package com.qq.tars.client.subset;


import com.qq.tars.common.util.StringUtils;
import com.qq.tars.context.DistributedContext;
import com.qq.tars.context.DistributedContextManager;
import com.qq.tars.support.log.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;

public class KeyConfig {

    private String defaultRoute;

    private List<KeyRoute> rules;

    private DistributedContext distributedContext = DistributedContextManager.getDistributedContext();

    private static final Logger logger = LoggerFactory.getClientLogger();

    public String findSubet(String routeKey){
        //非空校验
        if( routeKey == null || "".equals(routeKey) || rules == null){
            return null;
        }
        for ( KeyRoute rule: rules) {
            //根据根据分布式上下文信息获取 “请求的染色的key”
            String routeKeyReq;
            if( distributedContext != null){
                routeKeyReq = KeyRoute.getRouteKey(distributedContext);
            } else {
                logger.info("无分布式上下文信息distributedContext");
                return null;
            }
            //精确匹配
            if( "match".equals(rule.getAction())  ){
                if( routeKeyReq.equals(rule.getValue()) ){
                    return rule.getRoute();
                } else {
                    logger.info("染色key匹配不上，请求的染色key为：" + routeKeyReq + "; 规则的染色key为：" + rule.getValue());
                }
            }
            //正则匹配
            if( "equal".equals(rule.getAction()) ){
                if( StringUtils.matches(routeKeyReq, rule.getValue()) ){
                    return rule.getRoute();
                } else {
                    logger.info("正则匹配失败，请求的染色key为：" + routeKeyReq + "; 规则的染色key为：" + rule.getValue());
                }

            }
            //默认匹配
            if( "default".equals(rule.getAction()) ){
                //默认路由无需考虑染色key
                return rule.getRoute();
            }
        }
        return null;
    }

    public KeyConfig() {
    }

    public KeyConfig(String defaultRoute, List<KeyRoute> rules) {
        this.defaultRoute = defaultRoute;
        this.rules = rules;
    }

    public String getDefaultRoute() {
        return defaultRoute;
    }

    public void setDefaultRoute(String defaultRoute) {
        this.defaultRoute = defaultRoute;
    }

    public List<KeyRoute> getRules() {
        return rules;
    }

    public void setRules(List<KeyRoute> rules) {
        this.rules = rules;
    }
}
