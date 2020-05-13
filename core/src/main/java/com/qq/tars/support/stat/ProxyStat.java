/**
 * Tencent is pleased to support the open source community by making Tars available.
 * <p>
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 * <p>
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * https://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.qq.tars.support.stat;

import com.qq.tars.common.ClientVersion;
import com.qq.tars.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyStat {

    public static final List<Integer> DEFAULT_TIME_STAT_INTERVAL = new ArrayList<Integer>();

    static {
        DEFAULT_TIME_STAT_INTERVAL.add(5);
        DEFAULT_TIME_STAT_INTERVAL.add(10);
        DEFAULT_TIME_STAT_INTERVAL.add(50);
        DEFAULT_TIME_STAT_INTERVAL.add(100);
        DEFAULT_TIME_STAT_INTERVAL.add(200);
        DEFAULT_TIME_STAT_INTERVAL.add(500);
        DEFAULT_TIME_STAT_INTERVAL.add(1000);
        DEFAULT_TIME_STAT_INTERVAL.add(2000);
        DEFAULT_TIME_STAT_INTERVAL.add(3000);
        DEFAULT_TIME_STAT_INTERVAL.add(5000);
        DEFAULT_TIME_STAT_INTERVAL.add(10000);
        DEFAULT_TIME_STAT_INTERVAL.add(100000);
    }

    private ConcurrentHashMap<ProxyStatHead, ProxyStatBody> stat = new ConcurrentHashMap<ProxyStatHead, ProxyStatBody>();

    public void setTimeStatInterval(ProxyStatHead head, List<Integer> timeStatInterval) {
        if (stat.containsKey(head)) {
            stat.get(head).setTimeStatInterval(timeStatInterval);
        }
    }

    private ProxyStatBody getStatBody(ProxyStatHead head) {
        ProxyStatBody body = stat.get(head);
        if (body == null){
            stat.putIfAbsent(head, new ProxyStatBody(DEFAULT_TIME_STAT_INTERVAL));
        }
        return stat.get(head);
    }

    public void addInvokeTime(ProxyStatHead head, long costTimeMill, int result) {
        if (result == Constants.INVOKE_STATUS_SUCC) {
            getStatBody(head).onCallFinished(costTimeMill, Constants.INVOKE_STATUS_SUCC);
        } else if (result == Constants.INVOKE_STATUS_EXEC) {
            getStatBody(head).onCallFinished(costTimeMill, Constants.INVOKE_STATUS_EXEC);
        } else if (result == Constants.INVOKE_STATUS_TIMEOUT) {
            getStatBody(head).onCallFinished(costTimeMill, Constants.INVOKE_STATUS_TIMEOUT);
        }
    }

    public void addInvokeTimeByClient(String masterName, String slaveName, String slaveSetName, String slaveSetArea, String slaveSetID, String methodName,
                                      String slaveIp, int slavePort, int result, long costTimeMill) {
        ProxyStatHead head = new ProxyStatHead(masterName, slaveName, methodName, ProxyStatUtils.getLocalIP(), slaveIp, slavePort, result, slaveSetName, slaveSetArea, slaveSetID, "");
        addInvokeTime(head, costTimeMill, result);
    }

    public void addInvokeTimeByServer(String masterName, String application, String server, String slaveSetName, String slaveSetArea, String slaveSetID, String methodName,
                                      String masterIp, String slaveIp, int slavePort, int result, long costTimeMill) {
        String slaveName = slaveSetName != null ? String.format("%s.%s.%s%s%s", application, server, slaveSetName, slaveSetArea, slaveSetID) : String.format("%s.%s", application, server);
        ProxyStatHead head = new ProxyStatHead(masterName, slaveName, methodName, masterIp, slaveIp, slavePort, result, slaveSetName, slaveSetArea, slaveSetID, ClientVersion.getVersion());
        addInvokeTime(head, costTimeMill, result);
    }

    public List<Integer> getStatIntervals() {
        Collection<ProxyStatBody> collection = stat.values();
        if (!collection.isEmpty()) {
            for (ProxyStatBody body : collection) {
                if (body != null) {
                    return body.timeStatInterval;
                }
            }
        }
        return DEFAULT_TIME_STAT_INTERVAL;
    }

    public ConcurrentHashMap<ProxyStatHead, ProxyStatBody> getStats() {
        return this.stat;
    }

    public int size() {
        return this.stat.size();
    }

    public boolean isEmpty() {
        return this.stat.isEmpty();
    }
}
