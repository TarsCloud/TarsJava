package com.qq.tars.common.logger;


import org.slf4j.event.Level;

public interface LoggerFactory {
    /**
     * reload all logger config
     */
    int reloadConfig();

    /**
     * reset logger level
     * @param logName loggerName
     * @param level   set logger target log level
     */
    int setLoggerLevel(String logName, Level level);


    /**
     * start logger
     */
    void start();


    /**
     *  stop logger
     * */
    void stop();
}
