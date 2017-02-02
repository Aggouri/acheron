package com.dbg.cloud.acheron.zuul.filters.post;

import com.dbg.cloud.acheron.zuul.filters.AcheronFilter;

public abstract class PostFilter extends AcheronFilter {

    @Override
    public String filterType() {
        return "post";
    }
}
