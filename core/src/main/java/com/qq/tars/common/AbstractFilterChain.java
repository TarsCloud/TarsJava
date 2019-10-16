package com.qq.tars.common;

import com.qq.tars.net.core.Request;
import com.qq.tars.net.core.Response;

import java.util.Iterator;
import java.util.List;

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
    public void doFilter(Request request, Response response) throws Throwable {
        Filter filter = getFilter();
        if (filter != null) {
            filter.doFilter(request, response, this);
        } else {
            doRealInvoke(request, response);
        }

    }

    private Filter getFilter() {
        return iterator != null && iterator.hasNext() ? iterator.next() : null;
    }

    protected abstract void doRealInvoke(Request request, Response response) throws Throwable;

}
