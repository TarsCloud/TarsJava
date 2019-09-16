package com.qq.tars.support.log;

public class InnerLogger {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger();
    private static final InnerLogger instance = new InnerLogger();

    public static InnerLogger getLogger() {
        return instance;
    }

    // Logger(String name, Level level, String path, String pattern)
    // {
    // this(name, level, path, null, pattern);
    // }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void debug(String str) {
        logger.debug(str);
    }

    public void debug(String str, Throwable th) {
        if (logger.isDebugEnabled())
            logger.debug(str, th);

    }

    public void info(String str) {
        logger.info(str);
    }

    public void info(String str, Throwable th) {
        if (logger.isInfoEnabled())
            logger.info(str, th);
    }


    public void warn(String str) {
        logger.warn((str));
    }

    public void warn(String str, Throwable th) {
        if (logger.isWarnEnabled())
            logger.warn(str, th);
    }


    public void error(String str) {
        logger.error(str);
    }

    public void error(String str, Throwable th) {
        logger.error(str, th);
    }


    public void fatal(String str) {
        logger.error(str);
    }

    public void fatal(String str, Throwable th) {
        logger.error(str, th);
    }


}
