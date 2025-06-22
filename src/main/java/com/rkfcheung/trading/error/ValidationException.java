package com.rkfcheung.trading.error;

import org.springframework.lang.NonNull;

public class ValidationException extends IllegalStateException {

    private final ValidationError validationError;

    public ValidationException(@NonNull ValidationError validationError) {
        super(validationError.name());
        this.validationError = validationError;
    }

    public ValidationError error() {
        return validationError;
    }
}
