package com.rkfcheung.trading.error;

import org.springframework.lang.NonNull;

public enum ValidationError {
    MISSING_ORDER_TYPE,
    MISSING_INSTRUMENT,
    INVALID_PRICE,
    INVALID_QUANTITY;

    @NonNull
    public ValidationException asException() {
        return new ValidationException(this);
    }
}
