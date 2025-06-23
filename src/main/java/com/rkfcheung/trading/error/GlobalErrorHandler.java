package com.rkfcheung.trading.error;

import com.rkfcheung.trading.api.NewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationError(@NonNull ValidationException ex) {
        return ResponseEntity
                .badRequest()
                .body(NewResponse.rejected(ex.error()));
    }
}
