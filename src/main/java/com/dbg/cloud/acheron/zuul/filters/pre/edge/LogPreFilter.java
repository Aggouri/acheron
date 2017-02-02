package com.dbg.cloud.acheron.zuul.filters.pre.edge;

import com.dbg.cloud.acheron.zuul.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public final class LogPreFilter extends PreFilter {

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();
        final HttpServletRequest request = context.getRequest();

        log.info("{} request to {}", request.getMethod(), request.getRequestURL().toString());

        return null;
    }
}
