package com.impact.notificationconsumer.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.impact.notificationconsumer.exception.CustomException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalResponse {

    private boolean success;
    private String message;
    private Object data;

    public GlobalResponse(String message, Object data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }

    public GlobalResponse (CustomException ex) {
        this.success = false;
        this.message = ex.getMessage();
    }
}
