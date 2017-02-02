package com.dbg.cloud.acheron.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ValidationException extends Exception {

    public ValidationException(final String message) {
        super(message);
    }
}
