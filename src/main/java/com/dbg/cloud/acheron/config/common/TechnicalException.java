package com.dbg.cloud.acheron.config.common;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TechnicalException extends RuntimeException {

    public TechnicalException(final Exception exception) {
        super(exception);
    }
}
