/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.qq.tars.support.query;

import com.qq.tars.client.Communicator;
import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.subset.KeyRoute;
import com.qq.tars.client.subset.Subset;
import com.qq.tars.client.util.ParseTools;
import com.qq.tars.common.support.Holder;
import com.qq.tars.common.util.Constants;
import com.qq.tars.context.DistributedContextManager;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.support.query.prx.EndpointF;
import com.qq.tars.support.query.prx.QueryFPrx;

import java.util.*;

public final class QueryHelper {

    private final Communicator communicator;

    private Subset subset;

    public QueryHelper(Communicator communicator) {
        this.communicator = communicator;
    }

    public List<EndpointF> findObjectById(String objName) {
        return getPrx().findObjectById(objName);
    }

    private QueryFPrx getPrx() {
        return communicator.stringToProxy(QueryFPrx.class, communicator.getCommunicatorConfig().getLocator());
    }

    public String getServerNodes(ServantProxyConfig config) {
        QueryFPrx queryProxy = getPrx();
        String routeKey = getRouteKeyByContext();
        String name = config.getSimpleObjectName();
        Holder<List<EndpointF>> activeEp = new Holder<List<EndpointF>>(new ArrayList<EndpointF>());
        Holder<List<EndpointF>> inactiveEp = new Holder<List<EndpointF>>(new ArrayList<EndpointF>());
        int ret = TarsHelper.SERVERSUCCESS;
        if (config.isEnableSet()) {
            ret = queryProxy.findObjectByIdInSameSet(name, config.getSetDivision(), activeEp, inactiveEp);
        } else {
            ret = queryProxy.findObjectByIdInSameGroup(name, activeEp, inactiveEp);
        }

        if (ret != TarsHelper.SERVERSUCCESS) {
            return null;
        }
        Collections.sort(activeEp.getValue());

        //根据Subset规则过滤节点
        Holder<List<EndpointF>> activeEpFilter = subset.subsetEndpointFilter(name, routeKey, activeEp);

        StringBuilder value = new StringBuilder();
        if (activeEpFilter.value != null && !activeEpFilter.value.isEmpty()) {
            for (EndpointF endpointF : activeEpFilter.value) {
                if (value.length() > 0) {
                    value.append(":");
                }
                value.append(ParseTools.toFormatString(endpointF, true));
            }
        }
        if (value.length() < 1) {
            return null;
        }
        value.insert(0, Constants.TARS_AT);
        value.insert(0, name);
        return value.toString();
    }

    public String getRouteKeyByContext(){
        KeyRoute routeKey = new KeyRoute();
        return KeyRoute.getRouteKey(DistributedContextManager.getDistributedContext());
    }
}
