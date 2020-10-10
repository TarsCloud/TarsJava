package com.qq.tars.common.logger;


import org.slf4j.event.Level;

public interface LoggerFactory {

     String CLIENT_LOG_NAME = "TARS_CLIENT_LOGGER";
     String OM_LOG_NAME = "OM_LOGGER";

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
