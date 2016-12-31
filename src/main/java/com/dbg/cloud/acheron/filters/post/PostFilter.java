package com.dbg.cloud.acheron.filters.post;

import com.dbg.cloud.acheron.filters.AcheronFilter;

public abstract class PostFilter extends AcheronFilter {

    @Override
    public String filterType() {
        return "post";
    }
}
