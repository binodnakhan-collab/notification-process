package com.impact.notificationconsumer.exception;

import com.impact.notificationconsumer.payload.response.GlobalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<GlobalResponse> handleCustomException(CustomException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new GlobalResponse(ex));
    }
}
