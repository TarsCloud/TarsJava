package com.qq.tars.http.interceptor;


import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.common.support.Endpoint;
import com.qq.tars.common.util.Constants;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServantAdapterConfig;
import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.server.core.TarsServantProcessor;
import com.qq.tars.support.om.OmConstants;
import com.qq.tars.support.stat.InvokeStatHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/*
taf的http的被调的请求拦截器,做日志上报
服务名、接口名、耗时、错误码、主调ip(nginx的ip)、被调ip、被调set信息.
打印请求的流水日志/做上报
 */
public class HttpSvrTarsReportInterceptor implements HandlerInterceptor {

    static Logger log = LoggerFactory.getLogger(HttpSvrTarsReportInterceptor.class);

    public static final String METHOD_START_TIME = "methodStartTime";
    public static final String ARGU = "argu";   //argu占位符
    private static final String DEFAULT_HOST = "0.0.0.0";//

    String servantName = StringUtils.EMPTY;
    ServantAdapterConfig servantAdapterConfig = null;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(METHOD_START_TIME, startTime);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        try {
            if (!(handler instanceof HandlerMethod)) {  //可能会有一些跨域的预请求，不做处理.PreFlightHandler
                return;
            }
            String funcName = getFuncName(request, handler);
            long cost = getCost(request);
            int result = getResult(ex);
            String masterIp = request.getRemoteAddr();   //主调方的ip(nginx)
            String remark = "";  //暂定为空，实际上应该表示框架异常的错误码.
            printFlowLog(masterIp, funcName, cost, result, remark);

            if (StringUtils.isBlank(funcName)) {
                return;
            }

            //report http的被调指标
            ServerConfig serverConfig = ConfigurationManager.getInstance().getServerConfig();
            if (StringUtils.isBlank(servantName)) {
                findReportConstant(serverConfig);
                if (servantAdapterConfig == null) {
                    log.debug("servantAdapterConfig is null. please check");
                    return;
                }
            }

            String masterName = Constants.TARS_NOT_CLIENT;
            CommunicatorConfig communicatorConfig = serverConfig.getCommunicatorConfig();
            Endpoint serverEndpoint = servantAdapterConfig.getEndpoint();
            InvokeStatHelper.getInstance().addProxyStat(servantName).addInvokeTimeByServer(masterName, serverConfig.getApplication(), serverConfig.getServerName(), communicatorConfig.getSetName(),
                    communicatorConfig.getSetArea(), communicatorConfig.getSetID(), funcName, DEFAULT_HOST, serverEndpoint.host(), serverEndpoint.port(), result, cost);
        } catch (Throwable e) {
            log.error("httpSvr stat report exMsg:{}", e.getMessage(), e);
        }
    }

    public int getResult(@Nullable Exception ex) {
        int result = Constants.INVOKE_STATUS_SUCC; //默认为成功.
        if (ex != null) {
            log.info("req has exception. exMsg:{}", ex.getMessage(), ex);
            result = Constants.INVOKE_STATUS_EXEC;
        }
        return result;
    }

    public long getCost(HttpServletRequest request) {
        long endTime = System.currentTimeMillis();
        long startTime = (long) request.getAttribute(METHOD_START_TIME);
        return endTime - startTime;
    }

    /*
     * 几种获取方法名的方式：从requestMapping注解中获取/从请求头的request.getRequestURI()中获取/用代理的方法的方法名.
     * 最终选择从requestMappings中获取.
     * request.getRequestURI() 会有restful的请求，会导致func非常多，taf的监控上接口维度会非常多.
     * 代理方法的方法名，可能会重复，规则上没有限制.
     */
    private String getFuncName(HttpServletRequest request, Object handler) {
        String uri = "";
        HandlerMethod method = (HandlerMethod) handler;
        if (method.hasMethodAnnotation(RequestMapping.class)) {
            //找到类的mapping
            String controllerUri = "";
            String methodUri = "";
            RequestMapping controllerMapping = AnnotationUtils.findAnnotation(((HandlerMethod) handler).getBeanType(), RequestMapping.class);
            if (null != controllerMapping && controllerMapping.value() != null && controllerMapping.value().length > 0) {
                controllerUri = controllerMapping.value()[0];
            }

            RequestMapping requestMapping = method.getMethodAnnotation(RequestMapping.class);
            String[] values = requestMapping.value();
            if (requestMapping != null && requestMapping.value() != null && requestMapping.value().length > 0) {
                methodUri = values[0];
            } else {  //一些不存在的接口，会默认转到/error
                log.info("requestMethod not found.requestUri:{}", request.getRequestURI());
                return StringUtils.EMPTY;
            }
            uri = new StringBuilder().append(controllerUri).append(methodUri).toString();
        }

        String funcName = (uri.replaceFirst("/", "").replaceAll("/", "_"));
        return funcName;
    }

    public void findReportConstant(ServerConfig serverConfig) {
        LinkedHashMap<String, ServantAdapterConfig> servantAdapterConfigLinkedHashMap = serverConfig.getServantAdapterConfMap();
        for (Map.Entry<String, ServantAdapterConfig> entry : servantAdapterConfigLinkedHashMap.entrySet()) {  //取servant的ObjName作为上报的servant.
            if (!entry.getKey().equalsIgnoreCase(OmConstants.AdminServant)) {
                servantName = entry.getKey();
                servantAdapterConfig = entry.getValue();
                log.info("report servantName:{}", servantName);
                break;
            }
        }
    }

    //|主调方ip|接口名|请求参数|状态码|耗时|remark
    public static void printFlowLog(String masterIp, String funcName, long cost, int result, String remark) {
        if (result == TarsHelper.SERVERSUCCESS && (!TarsServantProcessor.isFlowLogEnable())) return;
        log.info("|{}|{}|{}|{}|{}|{}|",
                masterIp, funcName, ARGU, result, cost, remark);
    }
}
