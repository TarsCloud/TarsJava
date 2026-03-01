package com.qq.tars.support.trace.spi;

import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.server.config.ServerConfig;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

public class TraceInitializerManager {

    private static final Logger logger = Logger.getLogger(TraceInitializerManager.class.getName());

    private static final TraceInitializerManager INSTANCE = new TraceInitializerManager(loadInitializers());

    private final List<TraceInitializer> traceInitializers;

    TraceInitializerManager(List<TraceInitializer> traceInitializers) {
        if (traceInitializers == null || traceInitializers.isEmpty()) {
            this.traceInitializers = Collections.emptyList();
            return;
        }
        this.traceInitializers = Collections.unmodifiableList(new ArrayList<>(traceInitializers));
    }

    public static TraceInitializerManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        ServerConfig serverConfig = ConfigurationManager.getInstance().getServerConfig();
        if (serverConfig == null) {
            logger.warning("Skip trace initialization because server config is null");
            return;
        }
        init(serverConfig);
    }

    boolean init(ServerConfig serverConfig) {
        if (serverConfig.getSampleRate() <= 0) {
            return false;
        }

        for (TraceInitializer traceInitializer : traceInitializers) {
            if (!traceInitializer.supports(serverConfig)) {
                continue;
            }
            try {
                traceInitializer.init(serverConfig);
                logger.log(Level.INFO, "Initialized trace provider: {0}", traceInitializer.name());
                return true;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to initialize trace provider: " + traceInitializer.name(), e);
                return false;
            }
        }

        logger.log(Level.WARNING, "No trace initializer supports sampleType={0}.", serverConfig.getSampleType());
        return false;
    }

    static List<TraceInitializer> loadInitializers() {
        ServiceLoader<TraceInitializer> loaders = ServiceLoader.load(TraceInitializer.class);
        List<TraceInitializer> initializers = new ArrayList<>();
        for (TraceInitializer initializer : loaders) {
            initializers.add(initializer);
        }
        return initializers;
    }

    List<TraceInitializer> getTraceInitializers() {
        return traceInitializers;
    }
}
