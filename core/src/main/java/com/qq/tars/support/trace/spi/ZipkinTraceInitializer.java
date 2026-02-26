package com.qq.tars.support.trace.spi;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.support.om.OmConstants;
import com.qq.tars.support.trace.TraceManager;
import com.qq.tars.support.trace.exc.NotSupportedSuchSampleEncodingException;
import com.qq.tars.support.trace.exc.NotSupportedSuchSampleTypeException;
import io.opentracing.Tracer;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Encoding;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ZipkinTraceInitializer implements TraceInitializer {

    @Override
    public String name() {
        return "zipkin";
    }

    @Override
    public boolean supports(ServerConfig serverConfig) {
        if (serverConfig == null || serverConfig.getSampleType() == null) {
            return false;
        }

        String sampleType = serverConfig.getSampleType().trim().toLowerCase(Locale.ROOT);
        return "http".equals(sampleType)
                || "kafka08".equals(sampleType)
                || "kafka11".equals(sampleType)
                || "kafka".equals(sampleType);
    }

    @Override
    public void init(ServerConfig serverConfig) throws Exception {
        if (serverConfig.getSampleRate() <= 0) {
            return;
        }

        Sender sender = createSender(serverConfig);
        Reporter<zipkin2.Span> reporter = AsyncReporter.builder(sender).build();

        Map<String, Tracer> traces = new HashMap<>();
        for (String servant : serverConfig.getServantAdapterConfMap().keySet()) {
            if (!OmConstants.AdminServant.equals(servant)) {
                Tracing tracing = Tracing.newBuilder()
                        .localServiceName(servant)
                        .spanReporter(reporter)
                        .sampler(brave.sampler.Sampler.create(serverConfig.getSampleRate()))
                        .build();
                traces.put(servant, BraveTracer.create(tracing));
            }
        }

        TraceManager.getInstance().putTracers(traces);
    }

    private Sender createSender(ServerConfig serverConfig)
            throws NotSupportedSuchSampleTypeException, NotSupportedSuchSampleEncodingException {
        String sampleType = serverConfig.getSampleType().trim().toLowerCase(Locale.ROOT);
        String sampleAddress = requireSampleAddress(serverConfig.getSampleAddress());
        Encoding codec = createCodec(serverConfig);

        if ("http".equals(sampleType)) {
            String baseurl = normalizeHttpBaseAddress(sampleAddress);
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v2/spans";
            return URLConnectionSender.newBuilder().encoding(codec).endpoint(url).build();
        }

        if ("kafka08".equals(sampleType) || "kafka11".equals(sampleType) || "kafka".equals(sampleType)) {
            return zipkin2.reporter.kafka.KafkaSender.newBuilder().encoding(codec)
                    .bootstrapServers(sampleAddress)
                    .build();
        }

        throw new NotSupportedSuchSampleTypeException("unsupported sample type");
    }

    private String requireSampleAddress(String sampleAddress) {
        if (sampleAddress == null || sampleAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("sampleAddress is required for zipkin tracing");
        }
        return sampleAddress.trim();
    }

    private String normalizeHttpBaseAddress(String sampleAddress) {
        if (sampleAddress.contains("://")) {
            return sampleAddress;
        }
        return "http://" + sampleAddress;
    }

    private Encoding createCodec(ServerConfig serverConfig) throws NotSupportedSuchSampleEncodingException {
        String sampleEncoding = serverConfig.getSampleEncoding();
        if (sampleEncoding == null || sampleEncoding.trim().isEmpty() || "json".equalsIgnoreCase(sampleEncoding)) {
            return Encoding.JSON;
        }
        if ("proto".equalsIgnoreCase(sampleEncoding) || "proto3".equalsIgnoreCase(sampleEncoding)) {
            return Encoding.PROTO3;
        }
        throw new NotSupportedSuchSampleEncodingException("unsupported sample encoding");
    }
}
