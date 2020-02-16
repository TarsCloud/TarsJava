package com.tencent.logback.logserver.keying;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAwareBase;

import java.nio.ByteBuffer;

public class ContextNameKeyingStrategy extends ContextAwareBase implements KeyingStrategy<ILoggingEvent> {

    private byte[] contextNameHash = null;

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        final String hostname = context.getProperty(CoreConstants.CONTEXT_NAME_KEY);
        if (hostname == null) {
            addError("Hostname could not be found in context. HostNamePartitioningStrategy will not work.");
        } else {
            contextNameHash = ByteBuffer.allocate(4).putInt(hostname.hashCode()).array();
        }
    }

    @Override
    public byte[] createKey(ILoggingEvent e) {
        return contextNameHash;
    }
}
