package com.tencent.logback.logserver.keying;


public interface KeyingStrategy<E> {

    byte[] createKey(E e);

}
