package com.qq.tars.support.trace;

import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServerConfig;
import com.qq.tars.support.trace.spi.ZipkinTraceInitializer;

@Deprecated
public class TarsTraceZipkinConfiguration {

    private static final TarsTraceZipkinConfiguration INSTANCE = new TarsTraceZipkinConfiguration();

    private final ZipkinTraceInitializer zipkinTraceInitializer = new ZipkinTraceInitializer();

    private TarsTraceZipkinConfiguration() {
    }

    public static TarsTraceZipkinConfiguration getInstance() {
        return INSTANCE;
    }

    public void init() {
        ServerConfig serverConfig = ConfigurationManager.getInstance().getServerConfig();
        if (serverConfig == null || !zipkinTraceInitializer.supports(serverConfig)) {
            return;
        }

        try {
            zipkinTraceInitializer.init(serverConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
