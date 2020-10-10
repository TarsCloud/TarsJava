package com.qq.tars.logger;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.server.config.ConfigurationManager;
import org.slf4j.event.Level;

import java.io.File;

public class LoggerFactoryLogbackImpl implements com.qq.tars.common.logger.LoggerFactory {
    private LoggerContext logContext;

    @Override
    public int reloadConfig() {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(logContext);
        logContext.reset();
        try {
            File file = new File(ConfigurationManager.getInstance().getServerConfig().getBasePath() + "/conf/logback.xml");
            if (file.exists()) {
                configurator.doConfigure(file);
            } else {
                configurator.doConfigure(com.qq.tars.support.log.LoggerFactory.class.getResource("logback.xml"));
            }
            return 0;
        } catch (JoranException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int setLoggerLevel(String logName, Level level) {
        logContext.getLogger(logName).setLevel(ch.qos.logback.classic.Level.toLevel(level.name()));
        return 0;
    }

    @Override
    public void start() {
        logContext = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        String basePath = ConfigurationManager.getInstance().getServerConfig().getLogPath();
        if (basePath != null && basePath.length() > 0) {
            String level = ConfigurationManager.getInstance().getServerConfig().getLogLevel();
            if (StringUtils.isEmpty(level)) {
                level = Level.ERROR.name();
            }
            int maxHistory = ConfigurationManager.getInstance().getServerConfig().getLogMaxHistry();
            String appName = ConfigurationManager.getInstance().getServerConfig().getApplication();
            String serverName = ConfigurationManager.getInstance().getServerConfig().getServerName();
            initLogger(String.format("%s%s.%s.log", basePath, appName, serverName), Logger.ROOT_LOGGER_NAME, level, maxHistory);
            initLogger(String.format("%s%s.log", basePath, "tars_clent_om_log"), OM_LOG_NAME, level, maxHistory);
            initLogger(String.format("%s%s.log", basePath, "client_log"), CLIENT_LOG_NAME, level, maxHistory);
        }

    }

    private void initLogger(String file, String name, String level, int maxHistory) {
        LoggerContext loggerContext = logContext;
        Logger logger = loggerContext.getLogger(name);
        logger.detachAndStopAllAppenders();

        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("application");
        fileAppender.setFile(file);
        fileAppender.setAppend(true);

        TimeBasedRollingPolicy<ILoggingEvent> policy = new TimeBasedRollingPolicy<>();
        policy.setContext(loggerContext);
        policy.setMaxHistory(maxHistory);
        policy.setFileNamePattern(file + ".%d{yyyy-MM-dd}");
        policy.setParent(fileAppender);
        policy.start();
        fileAppender.setRollingPolicy(policy);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%date [%thread] %-5level %logger (%file:%line\\) - %msg%n");
        encoder.start();
        fileAppender.setEncoder(encoder);

        fileAppender.start();

        logger.addAppender(fileAppender);
        logger.setLevel(ch.qos.logback.classic.Level.toLevel(level));
        logger.setAdditive(false);
    }

    @Override
    public void stop() {
        logContext.stop();
    }
}
