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
import io.opentracing.Span;
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
            Span span = tracer.buildSpan(tarsServantRequest.getFunctionName())
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                    .start();
            try (Scope scope = tracer.activateSpan(span)) {
                Map<String, String> status = tarsServantRequest.getStatus();
                if (status == null) {
                    tarsServantRequest.setStatus(Maps.newHashMap());
                    status = tarsServantRequest.getStatus();
                }
                tracer.inject(span.context(), Format.Builtin.TEXT_MAP_INJECT, new TextMapInjectAdapter(status));
                span.setTag("client.ipv4", config.getLocalIP());
                span.setTag("client.port", config.getServantAdapterConfMap().get(servantName).getEndpoint().port());
                span.setTag("tars.interface", getObjName(servantName));
                span.setTag("tars.method", tarsServantRequest.getFunctionName());
                span.setTag("tars.protocol", protocol);
                span.setTag("tars.client.version", ClientVersion.getVersion());


                TarsServantResponse tarsServantResponse = (TarsServantResponse) response;
                try {
                    chain.doFilter(request, response);
                    if (isSync) {
                        span.setTag("tars.retcode", Integer.toString(tarsServantResponse.getRet()));
                    } else {
                        TraceManager.getInstance().putSpan(request.getRequestId(), tracer, span);
                    }
                } catch (Exception e) {
                    span.log(e.getMessage());
                    throw e;
                } finally {
                    if (isSync) {
                        span.finish();
                    }
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
