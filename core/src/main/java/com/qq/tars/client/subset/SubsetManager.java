package com.qq.tars.client.subset;

import com.qq.tars.common.support.Holder;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.support.query.prx.QueryFPrx;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubsetManager {

    private Map<String, SubsetConf> cache = new HashMap<>();

    private QueryFPrx queryProxy;

    //获取Subset路由规则，并存到subsetConf配置项
    public SubsetConf getSubsetConfig(String servantName){
        SubsetConf subsetConf = new SubsetConf();
        if( cache.containsKey(servantName) ){
            subsetConf = cache.get(servantName);

            //小于10秒从缓存中取
            if( Duration.between(subsetConf.getLastUpdate() , Instant.now()).toMillis() < 1000 ){
                return subsetConf;
            }
        }
        // get config from registry
        Holder<SubsetConf> subsetConfHolder = new Holder<SubsetConf>(subsetConf);
        int ret = queryProxy.findSubsetConfigById(servantName, subsetConfHolder);
        SubsetConf newSubsetConf = subsetConfHolder.getValue();
        if( ret == TarsHelper.SERVERSUCCESS ){
            return newSubsetConf;
        }
        //从registry中获取失败时，更新subsetConf添加进缓存
        subsetConf.setRuleType( newSubsetConf.getRuleType() );
        subsetConf.setLastUpdate( Instant.now() );
        cache.put(servantName, subsetConf);
        //解析subsetConf
        if( !newSubsetConf.isEnanle() ){
            subsetConf.setEnanle(false);
            return subsetConf;
        }
        if( "ratio".equals(newSubsetConf.getRuleType())){
            subsetConf.setRatioConf( newSubsetConf.getRatioConf() );
        } else {
            //按参数匹配
            KeyConfig newKeyConf = newSubsetConf.getKeyConf();
            List<KeyRoute> keyRoutes = newKeyConf.getRules();
            for ( KeyRoute kr: keyRoutes) {
                KeyConfig keyConf = new KeyConfig();
                //默认
                if("default".equals(kr.getAction())){
                    keyConf.setDefaultRoute(newKeyConf.getDefaultRoute());
                    subsetConf.setKeyConf(keyConf);
                }
                //精确匹配
                if("match".equals(kr.getAction())){
                    List<KeyRoute> rule = new ArrayList<>();
                    rule.add(new KeyRoute("match", kr.getValue() , kr.getRoute()));
                    keyConf.setRules( rule );
                }
                //正则匹配
                if("equal".equals(kr.getAction())){
                    List<KeyRoute> rule = new ArrayList<>();
                    rule.add(new KeyRoute("equal", kr.getValue() , kr.getRoute()));
                    keyConf.setRules( rule );
                }
            }
            subsetConf.setKeyConf(newKeyConf);
        }
        return subsetConf;
    }

    // 根据路由规则先获取到比例 / 染色路由的配置，再通过配置获取String的subset字段
    public String getSubset(String servantName, String routeKey){
        //check subset config exists
        SubsetConf subsetConf = getSubsetConfig(servantName);
        if( subsetConf == null ){
            return null;
        }
        // route key to subset
        if("ratio".equals(subsetConf.getRuleType())){
            RatioConfig ratioConf = subsetConf.getRatioConf();
            if(ratioConf != null){
                return ratioConf.findSubet(routeKey);
            }
        }
        KeyConfig keyConf = subsetConf.getKeyConf();
        if ( keyConf != null ){
            return keyConf.findSubet(routeKey);
        }
        return null;
    }

    public SubsetManager() {
    }

    public SubsetManager(Map<String, SubsetConf> cache) {
        if(cache == null){
            this.cache = new HashMap<>();
        }
    }

    public Map<String, SubsetConf> getCache() {
        return cache;
    }

    public void setCache(Map<String, SubsetConf> cache) {
        this.cache = cache;
    }

}
