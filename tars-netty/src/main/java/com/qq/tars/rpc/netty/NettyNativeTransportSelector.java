package com.qq.tars.rpc.netty;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;

final class NettyNativeTransportSelector {

    enum Transport {
        EPOLL,
        KQUEUE,
        NIO
    }

    private NettyNativeTransportSelector() {
    }

    static Transport select(boolean epollAvailable, boolean kqueueAvailable) {
        if (epollAvailable) {
            return Transport.EPOLL;
        }
        if (kqueueAvailable) {
            return Transport.KQUEUE;
        }
        return Transport.NIO;
    }

    static Transport current() {
        return select(Epoll.isAvailable(), KQueue.isAvailable());
    }

    static String unavailableCause() {
        Throwable epollCause = Epoll.unavailabilityCause();
        if (epollCause != null) {
            return "epoll unavailable: " + epollCause.getMessage();
        }
        Throwable kqueueCause = KQueue.unavailabilityCause();
        if (kqueueCause != null) {
            return "kqueue unavailable: " + kqueueCause.getMessage();
        }
        return "native transport unavailable";
    }
}
