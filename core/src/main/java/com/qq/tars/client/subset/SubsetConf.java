package com.qq.tars.client.subset;


import java.time.Instant;

public class SubsetConf {

    private boolean enanle;
    private String ruleType;
    private RatioConfig ratioConf;
    private KeyConfig keyConf;

    private Instant lastUpdate;

    public SubsetConf() {
        lastUpdate =  Instant.now();
    }


    public SubsetConf(boolean enanle, String ruleType, RatioConfig ratioConf, KeyConfig keyConf) {
        this.enanle = enanle;
        this.ruleType = ruleType;
        this.ratioConf = ratioConf;
        this.keyConf = keyConf;
        lastUpdate =  Instant.now();
    }

    public boolean isEnanle() {
        return enanle;
    }

    public void setEnanle(boolean enanle) {
        this.enanle = enanle;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public RatioConfig getRatioConf() {
        return ratioConf;
    }

    public void setRatioConf(RatioConfig ratioConf) {
        this.ratioConf = ratioConf;
    }

    public KeyConfig getKeyConf() {
        return keyConf;
    }

    public void setKeyConf(KeyConfig keyConf) {
        this.keyConf = keyConf;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
