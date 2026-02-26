package com.qq.tars.support.trace.spi;

import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.support.om.OmConstants;
import com.qq.tars.support.trace.TraceManager;
import io.opentracing.Tracer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SkywalkingTraceInitializer implements TraceInitializer {

    @Override
    public String name() {
        return "skywalking";
    }

    @Override
    public boolean supports(ServerConfig serverConfig) {
        if (serverConfig == null || serverConfig.getSampleType() == null) {
            return false;
        }

        String sampleType = serverConfig.getSampleType().trim().toLowerCase(Locale.ROOT);
        return "skywalking".equals(sampleType)
                || "sw".equals(sampleType)
                || "sw8".equals(sampleType);
    }

    @Override
    public void init(ServerConfig serverConfig) {
        if (serverConfig.getSampleRate() <= 0) {
            return;
        }

        String endpoint = normalizeEndpoint(serverConfig.getSampleAddress(), useHttpExporter(serverConfig.getSampleEncoding()));
        SpanExporter spanExporter = createSpanExporter(endpoint, serverConfig.getSampleEncoding());

        Map<String, Tracer> traces = new HashMap<>();
        for (String servant : serverConfig.getServantAdapterConfMap().keySet()) {
            if (OmConstants.AdminServant.equals(servant)) {
                continue;
            }

            Resource resource = Resource.getDefault().toBuilder()
                    .put(AttributeKey.stringKey("service.name"), servant)
                    .build();

            SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                    .setResource(resource)
                    .setSampler(Sampler.traceIdRatioBased(serverConfig.getSampleRate()))
                    .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                    .build();

            OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                    .setTracerProvider(tracerProvider)
                    .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                    .build();

            traces.put(servant, OpenTracingShim.createTracerShim(openTelemetrySdk));
        }

        TraceManager.getInstance().putTracers(traces);
    }

    private SpanExporter createSpanExporter(String endpoint, String sampleEncoding) {
        if (useHttpExporter(sampleEncoding)) {
            return OtlpHttpSpanExporter.builder().setEndpoint(endpoint).build();
        }
        return OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build();
    }

    private boolean useHttpExporter(String sampleEncoding) {
        if (sampleEncoding == null) {
            return false;
        }

        String encoding = sampleEncoding.trim().toLowerCase(Locale.ROOT);
        return "http".equals(encoding) || "otlp_http".equals(encoding);
    }

    private String normalizeEndpoint(String sampleAddress, boolean useHttpExporter) {
        if (sampleAddress == null || sampleAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("sampleAddress is required for skywalking tracing");
        }

        String endpoint = sampleAddress.trim();
        if (!endpoint.contains("://")) {
            endpoint = "http://" + endpoint;
        }

        if (!useHttpExporter) {
            return endpoint;
        }

        if (endpoint.endsWith("/v1/traces")) {
            return endpoint;
        }

        return endpoint + (endpoint.endsWith("/") ? "v1/traces" : "/v1/traces");
    }
}
