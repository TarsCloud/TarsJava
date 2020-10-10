package com.qq.tars.logger;

import com.qq.tars.common.logger.LoggerFactory;
import com.qq.tars.server.config.ConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.event.Level;

import java.io.File;
import java.net.URL;

public class LoggerFactoryLog4j2Impl implements LoggerFactory {

    private static final String NAME_LOGGER_FILE = System.getProperty("tars.logger.log4j2.name", "log4j2.xml");

    @Override
    public int reloadConfig() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        try {
            File logConfiguration = new File(ConfigurationManager.getInstance().getServerConfig().getBasePath() + "/conf/" + NAME_LOGGER_FILE);
            if (logConfiguration.exists()) {
                ctx.setConfigLocation(logConfiguration.toURI());
            } else {
                URL classpathConfiguration = com.qq.tars.support.log.LoggerFactory.class.getResource(NAME_LOGGER_FILE);
                if (classpathConfiguration != null) {
                    ctx.setConfigLocation(classpathConfiguration.toURI());
                }
            }
            return 0;
        } catch (Throwable e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int setLoggerLevel(String logName, Level level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.getLogger(logName).setLevel(org.apache.logging.log4j.Level.valueOf(level.name()));
        return 0;
    }

    @Override
    public void start() {
        // nope
    }

    @Override
    public void stop() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.stop();
    }
}
