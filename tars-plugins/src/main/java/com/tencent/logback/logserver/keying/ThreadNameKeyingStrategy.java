package com.tencent.logback.logserver.keying;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.nio.ByteBuffer;

public class ThreadNameKeyingStrategy implements KeyingStrategy<ILoggingEvent> {

    @Override
    public byte[] createKey(ILoggingEvent e) {
        return ByteBuffer.allocate(4).putInt(e.getThreadName().hashCode()).array();
    }
}
