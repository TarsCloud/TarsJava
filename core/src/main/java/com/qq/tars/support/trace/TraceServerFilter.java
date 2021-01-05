package com.qq.tars.support.trace;

import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import com.qq.tars.common.ClientVersion;
import com.qq.tars.common.Filter;
import com.qq.tars.common.FilterChain;
import com.qq.tars.common.support.Endpoint;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.server.config.ConfigurationManager;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TraceServerFilter implements Filter {

    private volatile boolean isTrace = false;

    @Override
    public void init() {
        isTrace = ConfigurationManager.getInstance().getServerConfig().getSampleRate() > 0;
    }

    @Override
    public void doFilter(Request request, Response response, FilterChain chain) throws Throwable {

    }

    @Override
    public CompletableFuture<Response> doFilter(Request request, FilterChain chain)
            throws Throwable {
        if (!isTrace || !(request instanceof TarsServantRequest)) {
            return chain.doFilter(request);
        } else {
            TarsServantRequest tarsServantRequest = (TarsServantRequest) request;
            try (TraceContext traceContext = TraceContext.getInstance().initCurrentTrace(tarsServantRequest.getServantName())) {
                Tracer tracer = TraceContext.getInstance().getCurrentTracer();
                Map<String, String> status = tarsServantRequest.getStatus();
                if (tracer == null || status == null || status.isEmpty()) {
                    return chain.doFilter(request);
                }
                try (Scope scope = tracer.buildSpan(tarsServantRequest.getFunctionName())
                        .asChildOf(tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(status)))
                        .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).startActive(true)) {
                    Endpoint endpoint = ConfigurationManager.getInstance().getServerConfig().getServantAdapterConfMap().get(tarsServantRequest.getServantName()).getEndpoint();
                    scope.span().setTag("server.ipv4", ConfigurationManager.getInstance().getServerConfig().getLocalIP());
                    if (endpoint != null) {
                        scope.span().setTag("server.port", endpoint.port());
                        if (StringUtils.isNotEmpty(endpoint.setDivision())) {
                            scope.span().setTag("tars.set_division", endpoint.setDivision());
                        }
                        scope.span().setTag("tars.server.version", ClientVersion.getVersion());
                    }
                    return chain.doFilter(request);
                }
            }
        }

    }

    @Override
    public void destroy() {


    }

}
