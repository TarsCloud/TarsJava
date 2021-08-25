package com.qq.tars.client.subset;


import com.qq.tars.common.support.Holder;
import com.qq.tars.support.query.prx.EndpointF;

import java.util.ArrayList;
import java.util.List;

public class Subset {

    private String hashString;

    private SubsetConf subsetConf;

    private KeyConfig keyConfig;
    private KeyRoute keyRoute;
    private RatioConfig ratioConfig;

    private SubsetManager subsetManager;


    //获取到规则后的subset，与节点的subset比较，过滤不匹配节点
    public Holder<List<EndpointF>> subsetEndpointFilter(String servantName, String routeKey, Holder<List<EndpointF>> eps){

        if( subsetConf==null || !subsetConf.isEnanle() ){
            return eps;
        }

        if(eps.value == null || eps.value.isEmpty()){
            return eps;
        }

        //调用subsetManager，根据比例/匹配等规则获取到路由规则的subset
        String subset = subsetManager.getSubset(servantName, routeKey);
        if( "".equals(subset) || subset == null){
            return eps;
        }
        //和每一个eps的subset比较，淘汰不符合要求的

        Holder<List<EndpointF>> epsFilter = new Holder<>(new ArrayList<EndpointF>());
        for (EndpointF ep : eps.value) {
            if( subset.equals(ep.getSubset())){
                epsFilter.getValue().add(ep);
            }
        }
        return epsFilter;
    }

    public Subset() {
    }

    public Subset(String hashString, SubsetConf subsetConf, KeyConfig keyConfig, KeyRoute keyRoute, RatioConfig ratioConfig) {
        this.hashString = hashString;
        this.subsetConf = subsetConf;
        this.keyConfig = keyConfig;
        this.keyRoute = keyRoute;
        this.ratioConfig = ratioConfig;
    }

    public String getHashString() {
        return hashString;
    }

    public void setHashString(String hashString) {
        this.hashString = hashString;
    }

    public SubsetConf getSubsetConf() {
        return subsetConf;
    }

    public void setSubsetConf(SubsetConf subsetConf) {
        this.subsetConf = subsetConf;
    }

    public KeyConfig getKeyConfig() {
        return keyConfig;
    }

    public void setKeyConfig(KeyConfig keyConfig) {
        this.keyConfig = keyConfig;
    }

    public KeyRoute getKeyRoute() {
        return keyRoute;
    }

    public void setKeyRoute(KeyRoute keyRoute) {
        this.keyRoute = keyRoute;
    }

    public RatioConfig getRatioConfig() {
        return ratioConfig;
    }

    public void setRatioConfig(RatioConfig ratioConfig) {
        this.ratioConfig = ratioConfig;
    }

    public SubsetManager getSubsetManager() {
        return subsetManager;
    }

    public void setSubsetManager(SubsetManager subsetManager) {
        this.subsetManager = subsetManager;
    }
}
