package com.dbg.cloud.acheron.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TechnicalException extends RuntimeException {

    public TechnicalException(final Exception exception) {
        super(exception);
    }
}
