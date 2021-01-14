package com.qq.tars.support.trace;

import com.google.common.collect.Maps;
import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import com.qq.tars.common.ClientVersion;
import com.qq.tars.common.Filter;
import com.qq.tars.common.FilterChain;
import com.qq.tars.common.util.Constants;
import com.qq.tars.context.DistributedContext;
import com.qq.tars.context.DistributedContextManager;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServerConfig;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.tag.Tags;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TraceClientFilter implements Filter {

    private boolean isTrace = false;

    @Override
    public void init() {
        isTrace = ConfigurationManager.getInstance().getServerConfig().getSampleRate() > 0;
    }

    @Override
    public void doFilter(Request request, Response response, FilterChain chain) throws Throwable {
        if (!isTrace) {
            chain.doFilter(request, response);
            return;
        }
        if (!(request instanceof TarsServantRequest) || !TraceUtil.checkServant(((TarsServantRequest) request).getServantName())) {
            chain.doFilter(request, response);
            return;
        }
        ServerConfig config = ConfigurationManager.getInstance().getServerConfig();
        DistributedContext context = DistributedContextManager.getDistributedContext();
        String servantName = context.get(TraceManager.INTERNAL_SERVANT_NAME);
        Tracer tracer = TraceContext.getInstance().getCurrentTracer();

        if (tracer == null) {
            chain.doFilter(request, response);
        } else {
            TarsServantRequest tarsServantRequest = (TarsServantRequest) request;
            boolean isSync = tarsServantRequest.getInvokeStatus() == Request.InvokeStatus.SYNC_CALL
                    || tarsServantRequest.getInvokeStatus() == Request.InvokeStatus.FUTURE_CALL;

            String protocol = Constants.TARS_PROTOCOL;
            Map<String, String> requestContext = tarsServantRequest.getContext();
            if (requestContext != null && !requestContext.isEmpty()) {
                protocol = requestContext.get(TraceManager.PROTOCOL).toString();
                requestContext.remove(TraceManager.PROTOCOL);
            }
            try (Scope scope = tracer.buildSpan(tarsServantRequest.getFunctionName()).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).startActive(isSync)) {
                Map<String, String> status = tarsServantRequest.getStatus();
                if (status == null) {
                    tarsServantRequest.setStatus(Maps.newHashMap());
                    status = tarsServantRequest.getStatus();
                }
                tracer.inject(scope.span().context(), Format.Builtin.TEXT_MAP, new TextMapInjectAdapter(status));
                scope.span().setTag("client.ipv4", config.getLocalIP());
                scope.span().setTag("client.port", config.getServantAdapterConfMap().get(servantName).getEndpoint().port());
                scope.span().setTag("tars.interface", getObjName(servantName));
                scope.span().setTag("tars.method", tarsServantRequest.getFunctionName());
                scope.span().setTag("tars.protocol", protocol);
                scope.span().setTag("tars.client.version", ClientVersion.getVersion());


                TarsServantResponse tarsServantResponse = (TarsServantResponse) response;
                try {
                    chain.doFilter(request, response);
                    if (isSync) {
                        scope.span().setTag("tars.retcode", Integer.toString(tarsServantResponse.getRet()));
                    } else {
                        TraceManager.getInstance().putSpan(request.getRequestId(), tracer, scope.span());
                    }
                } catch (Exception e) {
                    scope.span().log(e.getMessage());
                    throw e;
                }


            }
        }
    }

    @Override
    public CompletableFuture<Response> doFilter(Request request, FilterChain chain) throws Throwable {
        return null;
    }

    @Override
    public void destroy() {

    }

    private String getObjName(String fullName) {
        if (fullName == null || fullName.length() == 0) {
            return null;
        }
        String[] parts = fullName.split("\\.");
        if (parts.length < 3) {
            return fullName;
        }
        return parts[2];
    }

}
