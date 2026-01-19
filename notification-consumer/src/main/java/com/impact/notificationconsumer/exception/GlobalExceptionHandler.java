package com.impact.notificationconsumer.exception;

import com.impact.notificationconsumer.payload.response.GlobalResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<GlobalResponse> handleCustomException(CustomException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new GlobalResponse(ex));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GlobalResponse> handleException(NoResourceFoundException ex) {
        GlobalResponse globalResponse = new GlobalResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(globalResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalResponse> handleException(HttpMessageNotReadableException ex) {
        GlobalResponse globalResponse = new GlobalResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(globalResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GlobalResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = "Validation error: " + ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));
        GlobalResponse globalResponse = new GlobalResponse(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(globalResponse);
    }
}
