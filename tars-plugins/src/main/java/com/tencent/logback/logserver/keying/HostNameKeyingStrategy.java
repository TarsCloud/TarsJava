package com.tencent.logback.logserver.keying;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAwareBase;

import ch.qos.logback.core.spi.LifeCycle;
import java.nio.ByteBuffer;

public class HostNameKeyingStrategy extends ContextAwareBase implements KeyingStrategy<Object>, LifeCycle {

    private byte[] hostnameHash = null;
    private boolean errorWasShown = false;

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        final String hostname = context.getProperty(CoreConstants.HOSTNAME_KEY);
        if (hostname == null) {
            if (!errorWasShown) {
            addError("Hostname could not be found in context. HostNamePartitioningStrategy will not work.");
                errorWasShown = true;
            }
        } else {
            hostnameHash = ByteBuffer.allocate(4).putInt(hostname.hashCode()).array();
        }
    }

    @Override
    public byte[] createKey(Object e) {
        return hostnameHash;
    }

    @Override
    public void start() { }

    @Override
    public void stop() {
        errorWasShown = false;
    }

    @Override
    public boolean isStarted() { return true; }
}
