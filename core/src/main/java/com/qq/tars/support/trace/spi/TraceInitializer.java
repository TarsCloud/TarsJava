package com.qq.tars.support.trace.spi;

import com.qq.tars.server.config.ServerConfig;

public interface TraceInitializer {

    String name();

    boolean supports(ServerConfig serverConfig);

    void init(ServerConfig serverConfig) throws Exception;
}
