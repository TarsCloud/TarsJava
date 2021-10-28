package com.qq.tars.client.subset;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHash {

    SortedMap<Integer, String> virtualSubset;
    RatioConfig ratioConfig;
    Iterator<SortedMap.Entry<Integer,String>> it;
    SortedMap.Entry<Integer,String> entrySubset;

    public ConsistentHash() {
    }

    public void setVirtualSubset(Map<String, Integer> rules){
        SortedMap<Integer, String> virtualSubset = new TreeMap<Integer, String>();
        for(String subset : rules.keySet()){
            for(int i = 0; i < rules.get(subset); i++){
                String virtualSubsetName = subset + "&&" + String.valueOf(i);
                int hash = getHash(virtualSubsetName);
                virtualSubset.put(hash, virtualSubsetName);
            }
        }
        Iterator<SortedMap.Entry<Integer,String>> it = virtualSubset.entrySet().iterator();
        this.virtualSubset = virtualSubset;
        this.it = it;
    }

    public String getSubsetByVirtual(){
        if(entrySubset == null || !it.hasNext() || it == null){
            it = virtualSubset.entrySet().iterator();
            entrySubset = it.next();
        } else {
            entrySubset = it.next();
        }
        String virtualSubset = entrySubset.getValue();
        return virtualSubset.split("&&")[0];
    }

    private static int getHash(String str){
        final int p = 16777619;
        int hash = (int)2166136261L;
        for (int i = 0; i < str.length(); i++){
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }
}
