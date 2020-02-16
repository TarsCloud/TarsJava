package com.tencent.logback.logserver.keying;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.nio.ByteBuffer;

public class LoggerNameKeyingStrategy implements KeyingStrategy<ILoggingEvent> {

    @Override
    public byte[] createKey(ILoggingEvent e) {
        final String loggerName;
        if (e.getLoggerName() == null) {
            loggerName = "";
        } else {
            loggerName = e.getLoggerName();
        }
        return ByteBuffer.allocate(4).putInt(loggerName.hashCode()).array();
    }

}
