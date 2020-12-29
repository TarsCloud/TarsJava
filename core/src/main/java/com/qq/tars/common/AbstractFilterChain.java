package com.qq.tars.common;

import com.qq.tars.client.rpc.Request;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractFilterChain<T> implements FilterChain {

    protected String servant;
    protected FilterKind kind;
    protected T target;
    private Iterator<Filter> iterator;

    public AbstractFilterChain(List<Filter> filters, String servant, FilterKind kind, T target) {
        this.servant = servant;
        this.kind = kind;
        this.target = target;
        if (filters != null) {
            this.iterator = filters.iterator();
        }
    }

    @Override
    public CompletableFuture<TarsServantResponse> doFilter(Request request) throws Throwable {
        Filter filter = getFilter();
        if (filter != null) {
            return filter.doFilter(request, this);
        } else {
            return doRealInvoke(request);
        }

    }

    private Filter getFilter() {
        return iterator != null && iterator.hasNext() ? iterator.next() : null;
    }

    protected abstract CompletableFuture<TarsServantResponse> doRealInvoke(Request request) throws Throwable;

}
