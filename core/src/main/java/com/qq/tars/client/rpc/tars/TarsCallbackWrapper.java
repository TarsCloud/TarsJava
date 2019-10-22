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

package com.qq.tars.client.rpc.tars;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.cluster.ServantInvokerAliveChecker;
import com.qq.tars.common.Filter;
import com.qq.tars.common.FilterChain;
import com.qq.tars.common.FilterKind;
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.DyeingSwitch;
import com.qq.tars.context.DistributedContextManager;
import com.qq.tars.net.client.Callback;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.exc.TimeoutException;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.server.core.AppContextManager;
import com.qq.tars.support.log.LoggerFactory;
import com.qq.tars.support.stat.InvokeStatHelper;
import org.slf4j.Logger;

import java.util.List;

public class TarsCallbackWrapper implements Callback<TarsServantResponse> {
    private static final Logger logger = LoggerFactory.getClientLogger();

    private final String objName;
    private final ServantProxyConfig config;
    private final String methodName;
    private final String remoteIp;
    private final int remotePort;
    private final long bornTime;
    private final TarsServantRequest request;
    private final TarsInvoker invoker;
    private final Callback<TarsServantResponse> callback;
    private List<Filter> filters;

    public TarsCallbackWrapper(ServantProxyConfig config, String methodName, String remoteIp, int remotePort,
                               long bornTime, TarsServantRequest request, Callback<TarsServantResponse> callback, TarsInvoker invoker) {
        this.callback = callback;
        this.config = config;
        this.objName = config.getSimpleObjectName();

        this.methodName = methodName;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.bornTime = bornTime;
        this.request = request;
        this.filters = AppContextManager.getInstance().getAppContext() == null ? null : AppContextManager.getInstance().getAppContext().getFilters(FilterKind.CALLBACK);
        this.invoker = invoker;
    }

    public void onCompleted(TarsServantResponse response) {
        int ret = response.getRet() == TarsHelper.SERVERSUCCESS ? Constants.INVOKE_STATUS_SUCC : Constants.INVOKE_STATUS_EXEC;
        boolean available = ServantInvokerAliveChecker.isAlive(invoker.getUrl(), config, ret);
        invoker.setAvailable(available);
        try {
            beforeCallback();
            FilterChain filterChain = new TarsCallbackFilterChain(filters, objName, FilterKind.CALLBACK, callback, 0);
            filterChain.doFilter(request, response);
        } catch (Throwable ex) {
            ret = Constants.INVOKE_STATUS_EXEC;
            logger.error("error occurred on callback completed", ex);
            // fix: callback.onException() invoked in TarsAbstractCallback#doRealInvoke when execute exception, then throw ex
            // so callback.onException() will be called twice!
            // onException(ex);
        } finally {
            afterCallback();
            InvokeStatHelper.getInstance().addProxyStat(objName).addInvokeTimeByClient(config.getModuleName(), config.getSlaveName(), config.getSlaveSetName(), config.getSlaveSetArea(), config.getSlaveSetID(), methodName, remoteIp, remotePort, ret, System.currentTimeMillis() - bornTime);
        }
    }

    public void onException(Throwable e) {
        try {
            if (callback != null) {
                this.callback.onException(e);
            }
        } catch (Throwable ex) {
            logger.error("error occurred on callback exception", ex);
        }
    }

    public void onExpired() {
        int ret = Constants.INVOKE_STATUS_TIMEOUT;
        invoker.setAvailable(ServantInvokerAliveChecker.isAlive(invoker.getUrl(), config, ret));
        try {
            beforeCallback();
            FilterChain filterChain = new TarsCallbackFilterChain(filters, objName, FilterKind.CALLBACK, callback, 1);
            TarsServantResponse response = new TarsServantResponse(request.getIoSession());
            response.setRequest(request);
            response.setTicketNumber(request.getTicketNumber());
            response.setCause(new TimeoutException("async call timeout"));
            filterChain.doFilter(request, response);
        } catch (Throwable ex) {
            logger.error("error occurred on callback expired", ex);
        } finally {
            afterCallback();
            InvokeStatHelper.getInstance().addProxyStat(objName).addInvokeTimeByClient(config.getModuleName(), config.getSlaveName(), config.getSlaveSetName(), config.getSlaveSetArea(), config.getSlaveSetID(), methodName, remoteIp, remotePort, ret, System.currentTimeMillis() - bornTime);
        }
    }

    private void beforeCallback() {
        if (isDyeingReq()) {
            DyeingSwitch.enableUnactiveDyeing(request.getStatus().get(DyeingSwitch.STATUS_DYED_KEY), request.getStatus().get(DyeingSwitch.STATUS_DYED_FILENAME));
        }

    }

    private void afterCallback() {
        DistributedContextManager.releaseDistributedContext();
    }

    private boolean isDyeingReq() {
        return ((request.getMessageType() & TarsHelper.MESSAGETYPEDYED) == TarsHelper.MESSAGETYPEDYED) ? true : false;
    }
}
