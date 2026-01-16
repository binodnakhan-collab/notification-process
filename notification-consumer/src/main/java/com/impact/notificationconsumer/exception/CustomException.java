package com.impact.notificationconsumer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException{

    private final boolean success = false;
    private final HttpStatus status;

    public CustomException(String message, HttpStatus status, Object... params) {
        super(format(message,params));
        this.status = status;
    }

    private static String format(String message, Object... params) {
        return (params == null || params.length == 0)
                ? message
                : String.format(message, params);
    }
}
