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

package com.qq.tars.server.core;

import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import com.qq.tars.common.Filter;
import com.qq.tars.common.FilterChain;
import com.qq.tars.common.FilterKind;
import com.qq.tars.common.support.Endpoint;
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.DyeingKeyCache;
import com.qq.tars.common.util.DyeingSwitch;
import com.qq.tars.context.DistributedContext;
import com.qq.tars.context.DistributedContextManager;
import com.qq.tars.protocol.tars.support.TarsMethodInfo;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.exc.ServerDecodeException;
import com.qq.tars.rpc.exc.ServerException;
import com.qq.tars.rpc.exc.ServerNoServantException;
import com.qq.tars.rpc.exc.ServerQueueTimeoutException;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.support.om.OmServiceMngr;
import com.qq.tars.support.stat.InvokeStatHelper;
import com.qq.tars.support.trace.TraceManager;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

public class TarsServantProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(TarsServantProcessor.class);

    private static final String FLOW_SEP_FLAG = "|";
    private static final Random rand = new Random(System.currentTimeMillis());
    private static final Logger flowLogger = LoggerFactory.getLogger("tarsserver");

    public static void printServiceFlowLog(Logger logger, TarsServantRequest request, int status, long cost,
                                           String remark) {
        if (status == TarsHelper.SERVERSUCCESS && !isFlowLogEnable()) return;

        StringBuilder sb = new StringBuilder();
        Object args[] = request.getMethodParameters();
        int len = 25;

        sb.append(FLOW_SEP_FLAG);
        sb.append(request.getFunctionName()).append(FLOW_SEP_FLAG);

        if (null != args) {
            StringBuilder sbArgs = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    sbArgs.append("NULL").append(",");
                } else if (args[i] instanceof Number || args[i] instanceof Boolean) {
                    sbArgs.append(args[i]).append(",");
                } else {
                    sbArgs.append(encodeStringParam(args[i].toString(), len)).append(",");
                }
            }
            sbArgs = sbArgs.length() >= 1 ? sbArgs.deleteCharAt(sbArgs.length() - 1) : sbArgs;
            sb.append(sbArgs);
        }

        sb.append(FLOW_SEP_FLAG);
        sb.append(status).append(FLOW_SEP_FLAG).append(cost);
        sb.append(FLOW_SEP_FLAG).append(remark);

        logger.info(sb.toString());
    }

    public static boolean isFlowLogEnable() {
        return ConfigurationManager.getInstance().getServerConfig().getLogRate() - rand.nextInt(100) > 0;
    }

    private static String encodeStringParam(String longParam, int len) {
        if (longParam == null || longParam.length() == 0) return "";
        String shortParam = longParam;

        if (len > 0) {
            shortParam = longParam.length() > len ? longParam.substring(0, len) + "..(" + longParam.length() + ")" : longParam;
        }

        return shortParam.replaceAll(" ", "_").replaceAll(" ", "_").replaceAll("\n", "+").replace(',', '，').replace('(', '（').replace(')', '）');
    }

    @Override
    public Response process(Request req, Channel clientChannel) {
        TarsServantRequest request = null;
        TarsServantResponse response = null;
        ServantHomeSkeleton skeleton = null;
        AppContext appContext = null;
        ClassLoader oldClassLoader = null;
        int waitingTime = -1;
        long startTime = req.getBornTime();
        String remark = "";
        try {
            oldClassLoader = Thread.currentThread().getContextClassLoader();
            request = (TarsServantRequest) req;
            response = (TarsServantResponse) createResponse(request, clientChannel);
            response.setRequestId(request.getRequestId());
            if (TarsHelper.isPing(request.getFunctionName())) {
                return response;
            }

            if (response.getRet() != TarsHelper.SERVERSUCCESS) {
                throw new ServerDecodeException(response.getRet(), "decode error.");
            }

            int maxWaitingTimeInQueue = ConfigurationManager.getInstance().getServerConfig().getServantAdapterConfMap().get(request.getServantName()).getQueueTimeout();

            waitingTime = (int) (startTime - req.getBornTime());

            logger.debug("waittime:queu_time{},{},{},{}", waitingTime, maxWaitingTimeInQueue, startTime, req.getBornTime());

            if (waitingTime > maxWaitingTimeInQueue) {
                throw new ServerQueueTimeoutException(TarsHelper.SERVERQUEUETIMEOUT, "queue timeout.");
            }

            Context<?, ?> context = ContextManager.registerContext(request, response);
            context.setAttribute(Context.INTERNAL_START_TIME, startTime);
            context.setAttribute(Context.INTERNAL_CLIENT_IP, clientChannel.remoteAddress().toString());
            context.setAttribute(Context.INTERNAL_SERVICE_NAME, request.getServantName());
            context.setAttribute(Context.INTERNAL_METHOD_NAME, request.getFunctionName());
            context.setAttribute(Context.INTERNAL_SESSION_DATA, clientChannel);
            DistributedContext distributedContext = DistributedContextManager.getDistributedContext();
            distributedContext.put(DyeingSwitch.REQ, request);
            distributedContext.put(DyeingSwitch.RES, response);
            distributedContext.put(TraceManager.INTERNAL_SERVANT_NAME, request.getServantName());

            appContext = AppContextManager.getInstance().getAppContext();
            if (appContext == null) {
                throw new ServerNoServantException(TarsHelper.SERVERNOSERVANTERR, "empty appContext.");
            }

            preInvokeSkeleton();
            skeleton = appContext.getCapHomeSkeleton(request.getServantName());
            if (skeleton == null) {
                throw new ServerNoServantException(TarsHelper.SERVERNOSERVANTERR, "empty servantImp.");
            }
            List<Filter> filters = AppContextManager.getInstance().getAppContext().getFilters(FilterKind.SERVER);
            FilterChain filterChain = new TarsServerFilterChain(filters, request.getServantName(), FilterKind.SERVER, skeleton);
            filterChain.doFilter(request, response);
        } catch (Throwable cause) {
            cause.printStackTrace();
            System.err.println("ERROR: " + cause.getMessage());

            // 错误码
            int errCode = TarsHelper.SERVERUNKNOWNERR;
            if (cause instanceof ServerException) {
                errCode = ((ServerException) cause).getRet();
            } else if (cause instanceof InvocationTargetException) {
                errCode = TarsHelper.SERVERUNCATCHEDERR;
            } else if (cause instanceof IllegalArgumentException) {
                errCode = TarsHelper.SERVERDECODEERR;
            }

            if (response.isAsyncMode()) {
                try {
                    Context<TarsServantRequest, TarsServantResponse> context = ContextManager.getContext();
                    AsyncContext aContext = context.getAttribute(AsyncContext.PORTAL_CAP_ASYNC_CONTEXT_ATTRIBUTE);
                    if (aContext != null) aContext.writeException(cause);
                } catch (Exception ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                }
            } else {
                response.setResult(null);
                response.setCause(cause);
                response.setRet(errCode);
                remark = cause.toString();
            }
        } finally {
            if (oldClassLoader != null) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
            ContextManager.releaseContext();
            if (!response.isAsyncMode()) {
                printServiceFlowLog(flowLogger, request, response.getRet(), (System.currentTimeMillis() - startTime), remark);
            }
            postInvokeSkeleton();
            OmServiceMngr.getInstance().reportWaitingTimeProperty(waitingTime);
            reportServerStat(clientChannel, request, response, startTime);
        }
        return response;
    }

    @Override
    public void overload(Request req, Channel session) {
        TarsServantRequest request = (TarsServantRequest) req;
        TarsServantResponse response = (TarsServantResponse) createResponse(request, session);
        if (!TarsHelper.isPing(request.getFunctionName())) {
            if (response.getRet() == TarsHelper.SERVERSUCCESS) {
                response.setRet(TarsHelper.SERVEROVERLOAD);
            }
        }
        try {
            session.writeAndFlush(response);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private void reportServerStat(Channel channel, TarsServantRequest request, TarsServantResponse response, long startTime) {
        if (request.getVersion() == TarsHelper.VERSION2 || request.getVersion() == TarsHelper.VERSION3) {
            reportServerStat(channel, Constants.TARS_TUP_CLIENT, request, response, startTime);
        } else if (request.getMessageType() == TarsHelper.ONEWAY) {
            reportServerStat(channel, Constants.TARS_ONE_WAY_CLIENT, request, response, startTime);
        }
    }

    private void reportServerStat(Channel channel, String moduleName, TarsServantRequest request, TarsServantResponse response,
                                  long startTime) {
        ServerConfig serverConfig = ConfigurationManager.getInstance().getServerConfig();
        ServantAdapterConfig servantAdapterConfig = serverConfig.getServantAdapterConfMap().get(request.getServantName());
        if (servantAdapterConfig == null) {
            return;
        }
        CommunicatorConfig communicatorConfig = serverConfig.getCommunicatorConfig();
        Endpoint serverEndpoint = servantAdapterConfig.getEndpoint();
        String masterIp = ((InetSocketAddress) channel.remoteAddress()).getHostString();
        int result = response.getRet() == TarsHelper.SERVERSUCCESS ? Constants.INVOKE_STATUS_SUCC : Constants.INVOKE_STATUS_EXEC;
        InvokeStatHelper.getInstance().addProxyStat(request.getServantName()).addInvokeTimeByServer(moduleName, serverConfig.getApplication(), serverConfig.getServerName(), communicatorConfig.getSetName(), communicatorConfig.getSetArea(), communicatorConfig.getSetID(), request.getFunctionName(), (masterIp == null ? "0.0.0.0" : masterIp), serverEndpoint.host(), serverEndpoint.port(), result, (System.currentTimeMillis() - startTime));
    }

    private Response createResponse(TarsServantRequest request, Channel channel) {
        TarsServantResponse response = new TarsServantResponse(request.getRequestId());
        response.setRet(request.getRet());
        response.setVersion(request.getVersion());
        response.setPacketType(request.getPacketType());
        response.setMessageType(request.getMessageType());
        response.setStatus(request.getStatus());
        response.setRequest(request);
        response.setCharsetName(Charset.forName(request.getCharsetName()));
        response.setTimeout(request.getTimeout());
        response.setContext(request.getContext());
        return response;
    }

    public void preInvokeSkeleton() {
        DistributedContext distributedContext = DistributedContextManager.getDistributedContext();
        Request request = distributedContext.get(DyeingSwitch.REQ);
        if (request instanceof TarsServantRequest) {
            TarsServantRequest tarsServantRequest = (TarsServantRequest) request;
            initDyeing(tarsServantRequest);
        }
    }

    public void postInvokeSkeleton() {
        DistributedContext distributedContext = DistributedContextManager.getDistributedContext();
        distributedContext.clear();
    }

    private void initDyeing(TarsServantRequest request) {
        String routeKey;
        String fileName;
        if ((request.getMessageType() & TarsHelper.MESSAGETYPEDYED) == TarsHelper.MESSAGETYPEDYED) {
            routeKey = request.getStatus().get(DyeingSwitch.STATUS_DYED_KEY);
            fileName = request.getStatus().get(DyeingSwitch.STATUS_DYED_FILENAME);
            DyeingSwitch.enableUnactiveDyeing(routeKey, fileName);
            return;
        }
        String cache_routeKey = DyeingKeyCache.getInstance().get(request.getServantName(), request.getFunctionName());
        if (cache_routeKey == null) {
            cache_routeKey = DyeingKeyCache.getInstance().get(request.getServantName(), "DyeingAllFunctionsFromInterface");
        }
        if (cache_routeKey == null) {
            return;
        }
        TarsMethodInfo methodInfo = request.getMethodInfo();
        if (methodInfo.getRouteKeyIndex() != -1) {
            Object[] parameters = request.getMethodParameters();
            Object value = parameters[methodInfo.getRouteKeyIndex()];
            if (cache_routeKey.equals(value.toString())) {
                routeKey = cache_routeKey;
                fileName = ConfigurationManager.getInstance().getServerConfig().getServerName();
                DyeingSwitch.enableUnactiveDyeing(routeKey, fileName);
            }
        }
    }
}
