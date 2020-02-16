package com.tencent.logback.logserver.keying;

public class NoKeyKeyingStrategy implements KeyingStrategy<Object> {

    @Override
    public byte[] createKey(Object e) {
        return null;
    }
}
