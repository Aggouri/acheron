package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.introspection;

import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
public final class IntrospectionResult {

    @Delegate
    private final JSONResponse jsonResponse;

    public IntrospectionResult(@NonNull final JSONResponse jsonResponse) {
        this.jsonResponse = jsonResponse;
    }
}
